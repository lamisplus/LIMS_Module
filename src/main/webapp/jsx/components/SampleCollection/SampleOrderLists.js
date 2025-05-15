import React, { useEffect, useCallback, useState } from "react";
import { Card } from "react-bootstrap";
import Grid from "@material-ui/core/Grid";
import "./sample.css";
import { format } from "date-fns";
import uniq from "lodash/uniq";
import TextField from "@mui/material/TextField";
import Box from "@mui/material/Box";
import { LocalizationProvider } from "@mui/x-date-pickers-pro";
import { AdapterDayjs } from "@mui/x-date-pickers-pro/AdapterDayjs";
import { DateRangePicker } from "@mui/x-date-pickers-pro/DateRangePicker";
import { forwardRef } from "react";
import axios from "axios";
import { toast } from "react-toastify";
import { token, url } from "../../../api";
import MaterialTable from "material-table";
import * as Icons from "@material-ui/icons";

const tableIcons = Object.fromEntries(
  Object.entries(Icons).map(([key, Component]) => [
    key,
    forwardRef((props, ref) => <Component {...props} ref={ref} />),
  ])
);

const SampleSearch = (props) => {
  const [collectedSamples, setCollectedSamples] = useState([]);
  const [filteredSamples, setFilteredSamples] = useState([]);
  const [value, setValue] = useState([null, null]);
  const tableRef = React.createRef();

  const startDate = value[0]?.$d || null;
  const endDate = value[1]?.$d || null;

  const loadLabTestData = useCallback(async () => {
    try {
      const response = await axios.get(
        `${url}lims/collected-samples/?searchParam=*&pageNo=0&pageSize=100`,
        { headers: { Authorization: `Bearer ${token}` } }
      );
      const records = response.data.records || [];
      setCollectedSamples(records);
      setFilteredSamples(records);

      localStorage.removeItem("samples");
      localStorage.removeItem("manifest");
    } catch (e) {
      toast.error("An error occurred while fetching lab samples data");
    }
  }, []);

  useEffect(() => {
    loadLabTestData();
    props.setSubmitted(1);
  }, [loadLabTestData]);

  useEffect(() => {
    if (!startDate && !endDate) {
      setFilteredSamples(collectedSamples);
    } else {
      const filtered = collectedSamples.filter((sample) => {
        const date = new Date(sample.sampleCollectionDate);
        return (
          (!startDate || date >= startDate) && (!endDate || date <= endDate)
        );
      });
      setFilteredSamples(filtered);
    }
  }, [startDate, endDate, collectedSamples]);

  const calculateAge = (dob) =>
    new Date().getFullYear() - new Date(dob).getFullYear();

  const handleSampleChanges = (samples) => {
    const transformed = uniq(samples).map((item) => ({
      patientID: [
        { idNumber: item.patientId, idTypeCode: item.typecode },
        { idNumber: item.testId, idTypeCode: "CLIENTID" },
      ],
      firstName: item.firstname,
      surName: item.surname,
      sex: item.sex,
      age: calculateAge(item.dob),
      dateOfBirth: item.dob,
      sampleID: item.sampleId,
      sampleType: item.sampleType,
      indicationVLTest: 1,
      sampleOrderedBy: item.orderby,
      sampleOrderDate: item.orderbydate,
      sampleCollectedBy: item.collectedby,
      sampleCollectionDate: item.datecollected,
      sampleCollectionTime: item.timecollected,
      dateSampleSent: format(new Date(), "yyyy-MM-dd"),
      id: 0,
      manifestID: 0,
      pid: 0,
      priority: 0,
    }));

    transformed.sort((a, b) => {
//      const aSlash = a.sampleID?.includes("/");
//      const bSlash = b.sampleID?.includes("/");
//      if (aSlash && !bSlash) return 1;
//      if (!aSlash && bSlash) return -1;
//      if (!aSlash && !bSlash) return a.sampleID?.localeCompare(b.sampleID);
//      const [numA, denA] = a.sampleID.split("/").map(Number);
//      const [numB, denB] = b.sampleID.split("/").map(Number);
//      return numB - numA  || denB - denA;
        return a.sampleID?.localeCompare(b.sampleID, "en", { sensitivity: "base"});
    });

    localStorage.setItem("samples", JSON.stringify(transformed));
  };

  return (
    <div>
      <Card>
        <Card.Body>
          <Grid container spacing={2}>
            <LocalizationProvider
              dateAdapter={AdapterDayjs}
              localeText={{ start: "Start-Date", end: "End-Date" }}
            >
              <DateRangePicker
                value={value}
                onChange={(newValue) => setValue(newValue)}
                renderInput={(startProps, endProps) => (
                  <>
                    <TextField {...startProps} />
                    <Box sx={{ mx: 2 }}> to </Box>
                    <TextField {...endProps} />
                  </>
                )}
              />
            </LocalizationProvider>
          </Grid>
          <br />

          <MaterialTable
            icons={tableIcons}
            title={
              filteredSamples.length > 0
                ? "Sample Collection List"
                : "Loading Viral Load Samples..."
            }
            tableRef={tableRef}
            columns={[
              { title: "Type code", field: "typecode", hidden: true },
              { title: "Hospital ID", field: "patientId" },
              { title: "Test ID", field: "testId", hidden: true },
              { title: "First Name", field: "firstname", hidden: true },
              { title: "Surname", field: "surname", hidden: true },
              { title: "Sex", field: "sex", hidden: true },
              { title: "DOB", field: "dob", hidden: true },
              { title: "Age", field: "age", hidden: true },
              { title: "Test Type", field: "testType" },
              { title: "Phlebotomy No", field: "sampleId" },
              { title: "Sample Type", field: "sampleType" },
              { title: "Sample Orderby", field: "orderby" },
              { title: "Order Date", field: "orderbydate", type: "date" },
              { title: "Collected By", field: "collectedby" },
              {
                title: "Date Collected",
                field: "datecollected",
                type: "date",
                hidden: true,
              },
              {
                title: "Time Collected",
                field: "timecollected",
                type: "time",
                hidden: true,
              },
            ]}
            isLoading={collectedSamples.length === 0}
            data={filteredSamples.map((row) => ({
              typecode: row.patientID?.idTypeCode,
              patientId: row.patientID?.idNumber,
              testId: row.testID,
              firstname: row.firstName,
              surname: row.surName,
              sex: row.sex,
              dob: row.dateOfBirth,
              age: calculateAge(row.dateOfBirth),
              testType: "VL",
              sampleId: row.sampleID,
              sampleType: row.sampleType,
              orderby: row.sampleOrderedBy,
              orderbydate: row.sampleOrderDate,
              collectedby: row.sampleCollectedBy,
              datecollected: row.sampleCollectionDate,
              timecollected: row.sampleCollectionTime,
            }))}
            options={{
              headerStyle: {
                backgroundColor: "#014d88",
                color: "#fff",
                fontSize: "16px",
                padding: "10px",
              },
              selection: true,
              searchFieldAlignment: "left",
              pageSizeOptions: [10, 20, 100],
              pageSize: 10,
              debounceInterval: 400,
            }}
            onSelectionChange={handleSampleChanges}
          />
        </Card.Body>
      </Card>
    </div>
  );
};

export default SampleSearch;
