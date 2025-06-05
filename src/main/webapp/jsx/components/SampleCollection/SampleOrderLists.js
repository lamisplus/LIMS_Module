import React, {
  useEffect,
  useCallback,
  useState,
  useRef,
  forwardRef,
} from "react";
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

const SampleSearch = ({ setSubmitted }) => {
  const [collectedSamples, setCollectedSamples] = useState([]);
  const [filteredSamples, setFilteredSamples] = useState([]);
  const [dateRange, setDateRange] = useState([null, null]);
  const tableRef = useRef();

  const startDate = dateRange[0]?.$d || null;
  const endDate = dateRange[1]?.$d || null;


  const formatDate = (date) => {
    if (!date) return null;
    const inputDate = new Date(date);
    if (isNaN(inputDate.getTime())) return null;

    // Use local timezone formatting to prevent date shifts
    return format(inputDate, 'yyyy-MM-dd');
  };

  // const formatDate = (date) => {
  //   if (!date) return null;
  //   const inputDate = new Date(date);
  //   if (isNaN(inputDate.getTime())) return null;
  //   return inputDate.toISOString().split("T")[0];
  // };

  const calculateAge = (dob) =>
    dob ? new Date().getFullYear() - new Date(dob).getFullYear() : null;

  const loadLabTestData = useCallback(async (start, end) => {
    try {
      // const startParam = start ? `startDate=${start}&` : "";
      // const endParam = end ? `endDate=${end}&` : "";
      const params = [];
      if (start) params.push(`startDate=${start}`);
      if (end) params.push(`endDate=${end}`);
      params.push("pageNo=0", "pageSize=100");
      const queryParams = params.join("&");
     // const queryParams = `${startParam}${endParam}pageNo=0&pageSize=100`;
      const response = await axios.get(
        `${url}lims/lab-samples/pending?${queryParams}`,
        {
          headers: { Authorization: `Bearer ${token}` },
        }
      );

      const records = response.data.content || [];
      setCollectedSamples(records);
      setFilteredSamples(records);

      localStorage.removeItem("samples");
      localStorage.removeItem("manifest");
    } catch (e) {
      console.error(e);
      toast.error("An error occurred while fetching lab samples data");
    }
  }, []);

  useEffect(() => {
    loadLabTestData(null, null);
    setSubmitted(1);
  }, [loadLabTestData, setSubmitted]);

  useEffect(() => {
    const formattedStart = formatDate(startDate);
    let formattedEnd = formatDate(endDate);

    // If we have an end date, add one day to make it inclusive
    if (formattedEnd) {
      const endDateObj = new Date(endDate);
      endDateObj.setDate(endDateObj.getDate() + 1);
      formattedEnd = formatDate(endDateObj);
    }

    if (!formattedStart && !formattedEnd) {
      setFilteredSamples(collectedSamples);
    } else {
      if (formattedStart && formattedEnd) {
        loadLabTestData(formattedStart, formattedEnd);
      }
    }
  }, [startDate, endDate, loadLabTestData]);


  // useEffect(() => {
  //   const formattedStart = formatDate(startDate);
  //   const formattedEnd = formatDate(endDate);
  //
  //   if (!formattedStart && !formattedEnd) {
  //     setFilteredSamples(collectedSamples);
  //   } else {
  //     if (formattedStart && formattedEnd)
  //       loadLabTestData(formattedStart, formattedEnd);
  //   }
  // }, [startDate, endDate, loadLabTestData]);

  const handleSampleChanges = (samples) => {
    const transformed = uniq(samples).map((item) => ({
      patientID: [
        { idNumber: item.patientId, idTypeCode: "HOSPITALNO" },
        { idNumber: item.testId, idTypeCode: "CLIENTID" },
        { idNumber: item.uniqueId, idTypeCode: "RECENCY" },
      ],
      firstName: item.firstname,
      surName: item.surname,
      sex: item.sex,
      age: calculateAge(item.dob),
      dateOfBirth: item.dob,
      sampleID: item.sampleId,
      sampleType: item.sampleType,
      indicationVLTest: item.typecode,
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

    transformed.sort((a, b) =>
      a.sampleID?.localeCompare(b.sampleID, "en", { sensitivity: "base" })
    );

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
                value={dateRange}
                onChange={(newRange) => setDateRange(newRange)}
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
              {
                title: "VL Test Indication",
                field: "indicationVLTest",
                hidden: true,
              },
              { title: "Hospital ID", field: "patientId" },
              { title: "Unique ID", field: "uniqueId", hidden: true },
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
              typecode: row.indicationVLTest,
              patientId: row.hospitalNumber,
              uniqueId: row.patientID[2]?.idNumber,
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
