import React, { useEffect, useCallback, useState } from "react";
import { useHistory } from "react-router-dom";
import { Row, Col, Card, Table } from "react-bootstrap";

import SaveIcon from "@material-ui/icons/Save";
import DeleteIcon from "@mui/icons-material/Delete";

import Alert from "react-bootstrap/Alert";

import { Form, FormGroup, Input, Label } from "reactstrap";

import "./sample.css";
import axios from "axios";
import { toast } from "react-toastify";
import { token, url } from "../../../api";

import Button from "@mui/material/Button";

import { makeStyles } from "@material-ui/core/styles";

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
    border: "1px solid #014d88",
    borderRadius: "0px",
    fontSize: "14px",
    color: "#000",
  },
  error: {
    color: "#f85032",
    fontSize: "11px",
  },
  success: {
    color: "#4BB543 ",
    fontSize: "11px",
  },
  inputGroupText: {
    backgroundColor: "#014d88",
    fontWeight: "bolder",
    color: "#fff",
    borderRadius: "0px",
  },
  label: {
    fontSize: "14px",
    color: "#014d88",
    fontWeight: "600",
  },
}));

const Login = (props) => {
  let history = useHistory();

  const [errors, setErrors] = useState({});
  const [demo, setDemo] = useState(false);

  const classes = useStyles();
  const [loading, setLoading] = useState(true);
  const [login, setLogin] = useState({
    configName: "",
    serverUrl: "",
    configEmail: "",
    configPassword: "",
    testFacilityDATIMCode: "",
    testFacilityName: "",
  });

  const [logins, setLogins] = useState({});
  const [facilities, setFacilities] = useState([]);

  const Facilities = () => {
    axios
      .get(`${url}account`, {
        headers: { Authorization: `Bearer ${token}` },
      })
      .then((response) => {
        //console.log(response.data);
        setFacilities(response.data.applicationUserOrganisationUnits);
      })
      .catch((error) => {
        //console.log(error);
      });
  };

  const loadServerDetails = useCallback(async () => {
    try {
      const response = await axios.get(`${url}lims/config`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      //console.log("configs", response);
      setLogins(response.data);
      setLoading(false);
    } catch (e) {
      toast.error("An error occurred while fetching config details", {
        position: toast.POSITION.TOP_RIGHT,
      });
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    Facilities();
    loadServerDetails();
  }, [loadServerDetails]);

  const getFacilityDatim = (facilityId) => {
    console.log(facilityId);
  };

  const handleChange = (event) => {
    const { name, value } = event.target;
    //console.log(name, value)
    if (name === "testFacilityName") {
      getFacilityDatim(value);
    }

    if (name === "configName" && value === "Server") {
      setDemo(true);
    } else if (name === "configName" && value === "Live Server") {
      setDemo(false);
    }
    setLogin({ ...login, [name]: value });
  };

  const validateInputs = () => {
    let temp = { ...errors };
    temp.configName = login.configName ? "" : "Server Name is required.";
    temp.serverUrl = login.serverUrl ? "" : "Server URL is required.";
    temp.configEmail = login.configEmail ? "" : "Email is required.";
    temp.configPassword = login.configPassword
      ? ""
      : "Configuration password URL is required.";

    setErrors({
      ...temp,
    });
    return Object.values(temp).every((x) => x === "");
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      if (validateInputs()) {
        await axios
          .post(`${url}lims/config`, login, {
            headers: { Authorization: `Bearer ${token}` },
          })
          .then((resp) => {
            console.log("login details", resp);
            props.setConfig(resp.data);
            toast.success("LIMS Credentials saved successfully!!", {
              position: toast.POSITION.TOP_RIGHT,
            });

            setLogin({
              configName: "",
              serverUrl: "",
              configEmail: "",
              configPassword: "",
              facilityId: "",
              receivingPCRLabId: "",
            });
          });

        loadServerDetails();
        props.setKey("manifest-list");
      }
    } catch (e) {
      toast.error("An error occurred while saving LIMS Credentials", {
        position: toast.POSITION.TOP_RIGHT,
      });
      setLoading(false);
    }
    history.push("/");
  };

  const deleteConfig = async (e, id) => {
    e.preventDefault();
    try {
      const response = await axios.delete(`${url}lims/config/${id}`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      console.log(" delete config", response);
      loadServerDetails();
      props.setConfig({});
      toast.success("LIMS Credentials deleted successfully!!", {
        position: toast.POSITION.TOP_RIGHT,
      });
    } catch (e) {
      toast.error("An error occurred while deleting a config", {
        position: toast.POSITION.TOP_RIGHT,
      });
      setLoading(false);
    }
  };

  return (
    <div>
      <Card>
        <Card.Body>
          <hr />
          {
            <>
              <Alert
                style={{
                  width: "100%",
                  fontSize: "16px",
                  backgroundColor: "#014d88",
                  color: "#fff",
                  textAlign: "center",
                }}
              >
                <Alert.Heading>LIMS Configuration Details</Alert.Heading>
              </Alert>

              <Row>
                <Col xs={6} md={4}>
                  <Form>
                    <FormGroup>
                      <Label for="configName" className={classes.label}>
                        Server Name
                      </Label>
                      <select
                        className="form-control"
                        style={{
                          border: "1px solid #014d88",
                          borderRadius: "0px",
                          fontSize: "14px",
                          color: "#000",
                        }}
                        name="configName"
                        value={login.configName}
                        id="configName"
                        onChange={handleChange}
                      >
                        <option hidden>Select Configuration Server</option>
                        <option value="Server">LIMS Server</option>
                        {/* <option value="Live Server">Live Server</option> */}
                      </select>

                      {errors.configName !== "" ? (
                        <span className={classes.error}>
                          {errors.configName}
                        </span>
                      ) : (
                        ""
                      )}
                    </FormGroup>
                    <FormGroup>
                      <Label for="serverUrl" className={classes.label}>
                        LIMS URL
                      </Label>
                      <Input
                        type="text"
                        name="serverUrl"
                        id="serverUrl"
                        placeholder="Server URL"
                        className={classes.input}
                        onChange={handleChange}
                        value={login.serverUrl}
                      />
                      {errors.serverUrl !== "" ? (
                        <span className={classes.error}>
                          {errors.serverUrl}
                        </span>
                      ) : (
                        ""
                      )}
                    </FormGroup>
                    <FormGroup>
                      <Label for="configEmail" className={classes.label}>
                        LIMS Username
                      </Label>

                      <Input
                        type="text"
                        name="configEmail"
                        id="configEmail"
                        placeholder="LIMS Username"
                        className={classes.input}
                        onChange={handleChange}
                        value={login.configEmail}
                      />
                      {errors.configEmail !== "" ? (
                        <span className={classes.error}>
                          {errors.configEmail}
                        </span>
                      ) : (
                        ""
                      )}
                    </FormGroup>

                    <FormGroup>
                      <Label for="configPassword" className={classes.label}>
                        LIMS Password
                      </Label>

                      <Input
                        type="password"
                        name="configPassword"
                        id="configPassword"
                        placeholder="configuration password"
                        className={classes.input}
                        onChange={handleChange}
                        value={login.configPassword}
                      />

                      {errors.configPassword !== "" ? (
                        <span className={classes.error}>
                          {errors.configPassword}
                        </span>
                      ) : (
                        ""
                      )}
                    </FormGroup>
                    {demo === true ? (
                      <>
                        <FormGroup>
                          <Label
                            for="testFacilityName"
                            className={classes.label}
                          >
                            Facility Name
                          </Label>
                          <select
                            className="form-control"
                            name="testFacilityName"
                            id="testFacilityName"
                            onChange={handleChange}
                            style={{
                              border: "1px solid #014d88",
                              borderRadius: "0px",
                              fontSize: "14px",
                              color: "#000",
                            }}
                          >
                            <option value={""}></option>
                            {facilities.map((value) => (
                              <option
                                key={value.id}
                                value={value.organisationUnitName}
                              >
                                {value.organisationUnitName}
                              </option>
                            ))}
                          </select>

                          {/* <Input
                            type="text"
                            name="testFacilityName"
                            id="testFacilityName"
                            placeholder="Testing Facility Name"
                            className={classes.input}
                            onChange={handleChange}
                            value={login.testFacilityName}
                          /> */}
                        </FormGroup>
                        <FormGroup>
                          <Label
                            for="testFacilityDATIMCode"
                            className={classes.label}
                          >
                            Facility Datim Code
                          </Label>

                          <Input
                            type="text"
                            name="testFacilityDATIMCode"
                            id="testFacilityDATIMCode"
                            placeholder="Testing Facility Datim Code"
                            className={classes.input}
                            onChange={handleChange}
                            value={login.testFacilityDATIMCode}
                          />
                        </FormGroup>
                      </>
                    ) : (
                      ""
                    )}
                    <Button
                      variant="contained"
                      color="primary"
                      type="submit"
                      startIcon={<SaveIcon />}
                      onClick={handleSubmit}
                    >
                      Save
                    </Button>
                  </Form>
                </Col>
                <Col xs={6} md={8}>
                  <Table bordered size="sm" responsive>
                    <thead
                      style={{
                        backgroundColor: "#014d88",
                        color: "#fff",
                        textAlign: "center",
                      }}
                    >
                      <tr>
                        <th>S/N</th>
                        <th>Server Name</th>
                        <th>URL</th>
                        <th>Email</th>
                        {/*<th>Created Date</th>*/}
                        <th>Actions</th>
                      </tr>
                    </thead>
                    <tbody style={{ textAlign: "center" }}>
                      <tr key={logins.id}>
                        <td>{logins.id}</td>
                        <td>{logins.configName}</td>
                        <td>{logins.serverUrl}</td>
                        <td>{logins.configEmail}</td>
                        {/*<td>09/09/2022</td>*/}
                        <td>
                          <Button
                            variant="contained"
                            color="error"
                            startIcon={<DeleteIcon />}
                            onClick={(e) => deleteConfig(e, logins.id)}
                          ></Button>
                        </td>
                      </tr>
                    </tbody>
                  </Table>
                </Col>
              </Row>
            </>
          }
        </Card.Body>
      </Card>
    </div>
  );
};

export default Login;
