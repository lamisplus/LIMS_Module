import React, { useEffect, useCallback, useState } from "react";
import { Card } from "react-bootstrap";
import Grid from "@material-ui/core/Grid";
import "./sample.css";
import { format } from "date-fns";
import Alert from "react-bootstrap/Alert";
import uniq from "lodash/uniq";
import { Spinner } from "reactstrap";

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
import AddBox from "@material-ui/icons/AddBox";
import ArrowUpward from "@material-ui/icons/ArrowUpward";
import Check from "@material-ui/icons/Check";
import ChevronLeft from "@material-ui/icons/ChevronLeft";
import ChevronRight from "@material-ui/icons/ChevronRight";
import Clear from "@material-ui/icons/Clear";
import DeleteOutline from "@material-ui/icons/DeleteOutline";
import Edit from "@material-ui/icons/Edit";
import FilterList from "@material-ui/icons/FilterList";
import FirstPage from "@material-ui/icons/FirstPage";
import LastPage from "@material-ui/icons/LastPage";
import Remove from "@material-ui/icons/Remove";
import SaveAlt from "@material-ui/icons/SaveAlt";
import Search from "@material-ui/icons/Search";
import ViewColumn from "@material-ui/icons/ViewColumn";
import { makeStyles } from "@material-ui/core/styles";

const tableIcons = {
  Add: forwardRef((props, ref) => <AddBox {...props} ref={ref} />),
  Check: forwardRef((props, ref) => <Check {...props} ref={ref} />),
  Clear: forwardRef((props, ref) => <Clear {...props} ref={ref} />),
  Delete: forwardRef((props, ref) => <DeleteOutline {...props} ref={ref} />),
  DetailPanel: forwardRef((props, ref) => (
    <ChevronRight {...props} ref={ref} />
  )),
  Edit: forwardRef((props, ref) => <Edit {...props} ref={ref} />),
  Export: forwardRef((props, ref) => <SaveAlt {...props} ref={ref} />),
  Filter: forwardRef((props, ref) => <FilterList {...props} ref={ref} />),
  FirstPage: forwardRef((props, ref) => <FirstPage {...props} ref={ref} />),
  LastPage: forwardRef((props, ref) => <LastPage {...props} ref={ref} />),
  NextPage: forwardRef((props, ref) => <ChevronRight {...props} ref={ref} />),
  PreviousPage: forwardRef((props, ref) => (
    <ChevronLeft {...props} ref={ref} />
  )),
  ResetSearch: forwardRef((props, ref) => <Clear {...props} ref={ref} />),
  Search: forwardRef((props, ref) => <Search {...props} ref={ref} />),
  SortArrow: forwardRef((props, ref) => <ArrowUpward {...props} ref={ref} />),
  ThirdStateCheck: forwardRef((props, ref) => <Remove {...props} ref={ref} />),
  ViewColumn: forwardRef((props, ref) => <ViewColumn {...props} ref={ref} />),
};

const SampleSearch = (props) => {
  const [loading, setLoading] = useState("");
  const [collectedSamples, setCollectedSamples] = useState([]);
  const [manifestData, setManifestData] = useState([]);
  const [currentPage, setCurrentPage] = useState(1);
  const tableRef = React.createRef();
  const [config, setConfig] = useState([]);
  const [value, setValue] = React.useState([null, null]);

  let start_date = value[0] != null ? value[0].$d : null;
  let end_date = value[1] != null ? value[1].$d : null;

  const loadConfig = useCallback(async () => {
    try {
      const response = await axios.get(`${url}lims/configs`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      //console.log("configs", response);
      setConfig(response.data);
      setLoading(false);
    } catch (e) {
      toast.error("An error occurred while fetching config details", {
        position: toast.POSITION.TOP_RIGHT,
      });
      setLoading(false);
    }
  }, []);

  const loadLabTestData = useCallback(async () => {
    try {
      const response = await axios.get(
        `${url}lims/collected-samples/?searchParam=*&pageNo=0&pageSize=100`,
        { headers: { Authorization: `Bearer ${token}` } }
      );
      //console.log("samples", response);
      if (response.data.records === null) {
      } else {
        setCollectedSamples(response.data.records);
        setLoading(false);
      }

      localStorage.removeItem("samples");
      localStorage.removeItem("manifest");
    } catch (e) {
      toast.error("An error occurred while fetching lab samples data", {
        position: toast.POSITION.TOP_RIGHT,
      });
      setLoading(false);
    }
  }, []);

  const loadManifestData = useCallback(async () => {
    try {
      const response = await axios.get(
        `${url}lims/manifests?searchParam=*&pageNo=0&pageSize=100`,
        { headers: { Authorization: `Bearer ${token}` } }
      );

      let arr = [];
      if (response.data.records === null) {
      } else {
        response.data.records.forEach((x) => {
          x.sampleInformation.forEach((y) => {
            arr.push(y);
          });
        });
      }
      setManifestData(arr);
      setLoading(false);
    } catch (e) {
      toast.error("An error occurred while fetching manifest data", {
        position: toast.POSITION.TOP_RIGHT,
      });
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    setLoading("true");
    loadManifestData();
    loadLabTestData();
    loadConfig();
    props.setSubmitted(1);
  }, []);

  const calculate_age = (dob) => {
    var today = new Date();
    var birthDate = new Date(dob);
    var age_now = today.getFullYear() - birthDate.getFullYear();
    return age_now;
  };

  const handleSampleChanges = (sample) => {
    let samples = [];

    let uniqueSamples = uniq(sample).map((item) => {
      samples.push({
        patientID: [
          {
            idNumber: item.patientId,
            idTypeCode: item.typecode,
          },
        ],
        firstName: item.firstname,
        surName: item.surname,
        sex: item.sex === "M" ? "Male" : "Female",
        pregnantBreastFeedingStatus: "",
        age: 0,
        dateOfBirth: item.dob,
        age: item.age,
        sampleID: item.sampleId,
        sampleType: item.sampleType,
        indicationVLTest: 1,
        artCommencementDate: "",
        drugRegimen: "",
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
      });
    });

    localStorage.setItem("samples", JSON.stringify(samples));
  };

  const sampleFilter = (collectedSamples, manifestData) => {
    if (collectedSamples && manifestData) {
      return collectedSamples.filter((x) => {
        return !manifestData.some((y) => {
          return x.sampleID === y.sampleID;
        });
      });
    }
  };

  const values = sampleFilter(collectedSamples, manifestData);
  const handleChangePage = (page) => {
    setCurrentPage(page + 1);
  };

  const localization = {
    pagination: {
      labelDisplayedRows: `Page: ${currentPage}`,
    },
  };

  return (
    <div>
      {/* {collectedSamples.length <= 0 ? (
        <p>
          {" "}
          <Spinner color="primary" /> loading Patient Samples...
        </p>
      ) : (
        " "
      )} */}
      <Card>
        <Card.Body>
          <Grid container spacing={2}>
            <LocalizationProvider
              dateAdapter={AdapterDayjs}
              localeText={{ start: "Start-Date", end: "End-Date" }}
            >
              <DateRangePicker
                value={value}
                onChange={(newValue) => {
                  setValue(newValue);
                }}
                renderInput={(startProps, endProps) => (
                  <React.Fragment>
                    <TextField {...startProps} />
                    <Box sx={{ mx: 2 }}> to </Box>
                    <TextField {...endProps} />
                  </React.Fragment>
                )}
              />
            </LocalizationProvider>
          </Grid>
          <br />
          <MaterialTable
            icons={tableIcons}
            title="Sample Collection List"
            tableRef={tableRef}
            columns={[
              { title: "Type code", field: "typecode", hidden: true },
              { title: "Hospital ID", field: "patientId" },
              { title: "First Name", field: "firstname", hidden: true },
              { title: "Surname", field: "surname", hidden: true },
              { title: "Sex", field: "sex", hidden: true },
              { title: "DOB", field: "dob", hidden: true },
              { title: "Age", field: "age", hidden: true },
              {
                title: "Test Type",
                field: "testType",
              },
              { title: "Sample ID", field: "sampleId" },
              {
                title: "Sample Type",
                field: "sampleType",
              },
              { title: "Sample Orderby", field: "orderby" },
              {
                title: "Order Date",
                field: "orderbydate",
                type: "date",
                //hidden: true,
              },
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
            isLoading={loading}
            // data={handlePulledData}
            data={collectedSamples
              .filter((row) => {
                let filterPass = true;

                const date = new Date(row.sampleCollectionDate);

                if (start_date != null) {
                  filterPass = filterPass && new Date(start_date) <= date;
                }
                if (end_date != null) {
                  filterPass = filterPass && new Date(end_date) >= date;
                }
                return filterPass;
              })
              .map((row) => ({
                typecode: row.patientID.idTypeCode,
                patientId: row.patientID.idNumber,
                firstname: row.firstName,
                surname: row.surName,
                sex: row.sex === "M" ? "Male" : "Female",
                dob: row.dateOfBirth,
                age: calculate_age(row.dateOfBirth),
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
              searchFieldStyle: {
                width: "300%",
                margingLeft: "250px",
              },
              selection: true,
              filtering: false,
              exportButton: false,
              searchFieldAlignment: "left",
              pageSizeOptions: [10, 20, 100],
              pageSize: 10,
              debounceInterval: 400,
            }}
            onSelectionChange={(rows) => handleSampleChanges(rows)}
            onChangePage={handleChangePage}
            localization={localization}
          />
        </Card.Body>
      </Card>
    </div>
  );
};

export default SampleSearch;
