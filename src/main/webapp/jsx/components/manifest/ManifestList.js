import React, { useEffect, useCallback, useState } from "react";
import { Link } from "react-router-dom";
import MaterialTable from "material-table";
import { makeStyles } from "@material-ui/core/styles";
import { MdModeEdit } from "react-icons/md";
import MatButton from "@material-ui/core/Button";
import AddCardIcon from "@mui/icons-material/AddCard";

import SplitActionButton from "../SampleCollection/SplitActionButton";

import { Badge } from "reactstrap";

import "../SampleCollection/sample.css";

import { forwardRef } from "react";
import axios from "axios";
import { toast } from "react-toastify";
import { token, url } from "../../../api";

import "@reach/menu-button/styles.css";
import { FaEye } from "react-icons/fa";

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

const useStyles = makeStyles((theme) => ({
  card: {
    margin: theme.spacing(20),
    display: "flex",
    flexDirection: "column",
    alignItems: "center",
  },
  form: {
    width: "100%", // Fix IE 11 issue.
    marginTop: theme.spacing(3),
  },
  submit: {
    margin: theme.spacing(3, 0, 2),
  },
  cardBottom: {
    marginBottom: 20,
  },
  Select: {
    height: 45,
    width: 350,
  },
  button: {
    margin: theme.spacing(1),
  },

  root: {
    "& > *": {
      margin: theme.spacing(1),
    },
  },
  input: {
    display: "none",
  },
  error: {
    color: "#f85032",
    fontSize: "11px",
  },
  success: {
    color: "#4BB543 ",
    fontSize: "11px",
  },
}));

const DownloadManifest = (props) => {
  const classes = useStyles();
  const [loading, setLoading] = useState("");
  const [permissions, setPermissions] = useState([]);

  const [currentPage, setCurrentPage] = useState(1);

  const userPermission = () => {
    axios
      .get(`${url}account`, { headers: { Authorization: `Bearer ${token}` } })
      .then((response) => {
        //console.log("permission", response.data.permissions)
        setPermissions(response.data.permissions);
      })
      .catch((error) => {});
  };

  const loadConfig = useCallback(async () => {
    try {
      const response = await axios.get(`${url}lims/config`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      console.log("configs", response);
      props.setConfig(response.data);
      setLoading(false);
    } catch (e) {
      toast.error("An error occurred while fetching config details", {
        position: toast.POSITION.TOP_RIGHT,
      });
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    setLoading("true");
    loadConfig();
    userPermission();
  }, [loadConfig]);

  const actionItems = (row) => {
    return [
      {
        name: "View",
        type: "link",
        icon: <FaEye size="22" />,
        to: {
          pathname: "/print-manifest",
          state: { sampleObj: row, permissions: permissions },
        },
      },
      {
        ...(row.manifestStatus === "Ready"
          ? " "
          : {
              name: " Results",
              type: "link",
              icon: <FaEye size="20" color="rgb(4, 196, 217)" />,
              to: {
                pathname: "/result",
                state: { manifestObj: row, permissions: permissions },
              },
            }),
      },
      {
        ...(row.manifestStatus === "Ready"
          ? " "
          : {
              name: "Add RSL Result",
              type: "link",
              icon: <MdModeEdit size="20" color="rgb(4, 196, 217)" />,
              to: {
                pathname: "/add-result",
                state: { manifestObj: row, permissions: permissions },
              },
            }),
      },
    ];
  };

  const handlePulledData = (query) =>
    new Promise((resolve, reject) => {
      axios
        .get(
          `${url}lims/manifests?searchParam=${query.search}&pageNo=${query.page}&pageSize=${query.pageSize}`,
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
                pickupDate: row.dateScheduledForPickup.replace("T", " "),
                createDate: row.createDate.replace("T", " "),
                lab: row.receivingLabName,
                packaged_by: row.samplePackagedBy,
                samples: row.sampleInformation.length,
                results:
                  row.results.length !== 0 ? (
                    <Badge color="info">{row.results.length}</Badge>
                  ) : (
                    0
                  ),
                status:
                  row.manifestStatus === "Ready" ? (
                    <Badge color="secondary">Not Submitted</Badge>
                  ) : (
                    <Badge
                      color="#013220"
                      style={{ backgroundColor: "#006400", color: "#fff" }}
                    >
                      {row.manifestStatus}
                    </Badge>
                  ),
                actions: (
                  <>
                    <SplitActionButton actions={actionItems(row)} />
                  </>
                ),
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
      <div>
        {Object.keys(props.config).length !== 0 ? (
          <p style={{ textAlign: "right" }}>
            <Link color="inherit" to={{ pathname: "/create-manifest" }}>
              <MatButton
                variant="contained"
                color="primary"
                startIcon={<AddCardIcon />}
                style={{ backgroundColor: "#014d88", color: "#fff" }}
              >
                Create Manifest
              </MatButton>
            </Link>
          </p>
        ) : (
          ""
        )}
        <MaterialTable
          icons={tableIcons}
          title="Previous Manifests"
          columns={[
            { title: "Manifest Id", field: "manifestId" },
            { title: "Pickup Date", field: "pickupDate" },
            { title: "Created Date", field: "createDate" },
            { title: "Receiving Lab", field: "lab" },
            { title: "Packaged By", field: "packaged_by" },
            { title: "Total Samples", field: "samples" },
            { title: "Total Results", field: "results" },
            { title: "Status", field: "status" },
            { title: "Action", field: "actions" },
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
            exportButton: false,
            searchFieldAlignment: "left",
            pageSizeOptions: [10, 20, 100],
            pageSize: 10,
            debounceInterval: 400,
          }}
          onChangePage={handleChangePage}
          localization={localization}
        />
      </div>
    </>
  );
};

export default DownloadManifest;
