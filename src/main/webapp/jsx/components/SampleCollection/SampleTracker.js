import React, { useState, forwardRef } from "react";
import MaterialTable from "material-table";
import axios from "axios";
import { token, url } from "../../../api";
import Grid from "@material-ui/core/Grid";
import { Badge } from "reactstrap";
import Button from "@mui/material/Button";
import TextField from "@mui/material/TextField";
import Box from "@mui/material/Box";
import { LocalizationProvider } from "@mui/x-date-pickers-pro";
import { AdapterDayjs } from "@mui/x-date-pickers-pro/AdapterDayjs";
import { DateRangePicker } from "@mui/x-date-pickers-pro/DateRangePicker";
import DownloadIcon from "@mui/icons-material/Download";

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

const SampleTracker = () => {
  const [loading, setLoading] = useState("");
  const [currentPage, setCurrentPage] = useState(1);
  const [value, setValue] = React.useState([null, null]);

  let start_date = value[0] != null ? value[0].$d : null;
  let end_date = value[1] != null ? value[1].$d : null;

  const handleDownload = () => {
    if (start_date != null) {
      start_date = new Date(start_date);
    }
    if (end_date != null) {
      end_date = new Date(end_date);
    }

    console.log("report", start_date, end_date);
  };

  const sampleStatus = (e) => {
    //console.log(e)
    if (parseInt(e) === 1) {
      return (
        <p>
          <Badge color="success">Result available</Badge>
        </p>
      );
    } else if (parseInt(e) === 2) {
      return (
        <p>
          <Badge color="danger">Rejected</Badge>
        </p>
      );
    } else if (parseInt(e) === 3) {
      return (
        <p>
          <Badge color="info">In-Progress</Badge>
        </p>
      );
    } else if (parseInt(e) === 4) {
      return (
        <p>
          <Badge color="warning">Re-Run</Badge>
        </p>
      );
    } else if (parseInt(e) === 5) {
      return (
        <p>
          <Badge color="warning">Transferred</Badge>
        </p>
      );
    } else {
      return (
        <p>
          <Badge color="dark">Result Pending</Badge>
        </p>
      );
    }
  };

  const handlePulledData = (query) =>
    new Promise((resolve, reject) => {
      axios
        .get(
          `${url}lims/manifests-all-in-one-drkarim?searchParam=${query.search}&pageNo=${query.page}&pageSize=${query.pageSize}`,
          { headers: { Authorization: `Bearer ${token}` } }
        )
        .then((resp) => resp)
        .then((result) => {
          if (result.data.records === null) {
            resolve({
              data: [],
              page: 0,
              totalCount: 0,
            });
          } else {
            resolve({
              data: result.data.records.map((row) => ({
                manifestId: row.manifestID,
                patientId: row.patientID[0].idNumber,
                sampleId: row.sampleID,
                sampleType: row.sampleType,
                orderBy: row.sampleOrderedBy,
                sampleOrderDate: row.sampleOrderDate,
                sampleCollectedBy: row.sampleCollectedBy,
                sampleCollectionDate: row.sampleCollectionDate,
                dateSampleSent: row.dateSampleSent,
                lab: row.receivingLabName,
                pcr_no: row.pcrLabSampleNumber,
                status:
                  row.manifestStatus === "Ready" ? (
                    <Badge color="secondary">Not Submitted</Badge>
                  ) : (
                    <Badge
                      color="#013220"
                      style={{ backgroundColor: "#006400", color: "#fff" }}
                    >
                      Submitted
                    </Badge>
                  ),
                resultDate: row.resultDate,
                approvalDate: row.approvalDate,
                assayDate: row.assayDate,
                tested_by: row.testedBy,
                approved_by: row.approvedBy,
                receivedDate: row.dateResultDispatched,
                testResult:
                  row.testResult !== null ? `${row.testResult} cp\mL` : "",
              })),
              page: query.page,
              totalCount: result.data.totalRecords,
            });
          }
        });
    });

  const handleChangePage = (page) => {
    setCurrentPage(page + 1);
  };

  const localization = {
    pagination: {
      labelDisplayedRows: `Page: ${currentPage}`,
    },
  };

  return (
    <>
      <br />
      {/* <Grid container spacing={2}>
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
          </Grid> */}
      <hr />
      <MaterialTable
        icons={tableIcons}
        title="PCR Sample Tracker"
        columns={[
          { title: "Status", field: "status" },
          { title: "Manifest Id", field: "manifestId" },
          { title: "Patient Id", field: "patientId" },
          { title: "Sample Id", field: "sampleId" },
          { title: "Sample Type", field: "sampleType" },
          { title: "Order by", field: "orderBy" },
          { title: "Order Date", field: "sampleOrderDate" },
          { title: "Collected By", field: "sampleCollectedBy" },
          { title: "Collection Date", field: "sampleCollectionDate" },
          { title: "Date Sent", field: "dateSampleSent" },
          { title: "PCR_Lab", field: "lab" },
          { title: "PCR_No", field: "pcr_no" },

          { title: "Result Date", field: "resultDate" },
          { title: "Approval Date", field: "approvalDate" },
          { title: "Assay_Date", field: "assayDate" },
          { title: "Tested By", field: "tested_by" },
          { title: "Approved By", field: "approved_by" },
          { title: "Receive Date", field: "receivedDate" },
          { title: "Sample Result", field: "testResult" },
        ]}
        isLoading={loading}
        data={handlePulledData}
        options={{
          headerStyle: {
            backgroundColor: "#014d88",
            color: "#fff",
            fontSize: "16px",
            padding: "10px",
          },
          searchFieldStyle: {
            width: "200%",
            margingLeft: "250px",
          },
          selection: false,
          filtering: false,
          exportButton: true,
          searchFieldAlignment: "left",
          pageSizeOptions: [10, 20, 100],
          pageSize: 10,
          debounceInterval: 400,
        }}
        onChangePage={handleChangePage}
        localization={localization}
      />
    </>
  );
};

export default SampleTracker;
