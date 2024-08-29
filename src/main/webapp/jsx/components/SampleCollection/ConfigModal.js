import React, { useState, useEffect, useCallback } from "react";
import {
  Modal,
  ModalHeader,
  ModalBody,
  Form,
  Row,
  Col,
  Input,
  FormGroup,
  Label,
  Card,
  CardBody,
} from "reactstrap";
import axios from "axios";

import MatButton from "@material-ui/core/Button";
import { makeStyles } from "@material-ui/core/styles";
import SaveIcon from "@material-ui/icons/Save";
import SendIcon from "@mui/icons-material/Send";
import CancelIcon from "@material-ui/icons/Cancel";
import "react-toastify/dist/ReactToastify.css";
import "react-widgets/styles.css";
import { token, url } from "../../../api";
import { toast } from "react-toastify";

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
    border: "2px solid #014d88",
    borderRadius: "0px",
    fontSize: "16px",
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
    fontSize: "16px",
    color: "rgb(153, 46, 98)",
    fontWeight: "600",
  },
}));

const ConfigModal = (props) => {
  const { manifestsId } = props;

  const classes = useStyles();
  const [saved, setSaved] = useState(false);
  const [loading, setLoading] = useState(false);
  const [visible, setVisible] = useState(true);
  const onDismiss = () => setVisible(false);

  const [logins, setLogins] = useState({});

  const [configId, setConfigId] = useState(0);

  const loadConfig = useCallback(async () => {
    try {
      const response = await axios.get(`${url}lims/config`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      setLogins(response.data);
      localStorage.setItem("configId", JSON.stringify(response.data.id));
      setLoading(false);
    } catch (e) {
      toast.error("An error occurred while fetching config details", {
        position: toast.POSITION.TOP_RIGHT,
      });
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadConfig();
  }, [loadConfig]);

  const handleChange = (event) => {
    const { name, value } = event.target;
    setConfigId(parseInt(value));
    setSaved(true);
  };

  const saveSample = async (e) => {
    e.preventDefault();
    //console.log(configId)
    //setSaved(true);
    toast.success("Sample manifest saved successfully!!", {
      position: toast.POSITION.TOP_RIGHT,
    });

    props.togglestatus();
  };

  const sendManifest = async (e) => {
    e.preventDefault();

    const timer = setInterval(() => {
      props.handleProgress((prevProgress) =>
        prevProgress >= 100 ? 100 : prevProgress + 2
      );
    }, 500);

    const serverId = JSON.parse(localStorage.getItem("configId"));
    props.togglestatus();
    try {
      await axios
        .get(`${url}lims/ready-manifests/${manifestsId}/${serverId}`, {
          headers: { Authorization: `Bearer ${token}` },
        })
        .then((resp) => {
          if (resp) {
            console.log("sending manifest", resp);
            props.handleProgress(100);

            toast.success("Sample manifest sent successfully to PCR Lab.", {
              position: toast.POSITION.TOP_RIGHT,
            });

            props.submitted(2);
            props.previous(0);
          }
        })
        .catch((err) => {
          clearInterval(timer);
          console.log("err", err);
          toast.error("Poor Internet Connection....", {
            position: toast.POSITION.TOP_RIGHT,
          });

          props.handleOpen();
        });
    } catch (err) {
      //props.setFailed(true);

      clearInterval(timer);
      toast.error("Error encountered while sending manifest", {
        position: toast.POSITION.TOP_RIGHT,
      });

      props.handleOpen();
    }
  };

  const resendManifest = async (e) => {
    e.preventDefault();

    props.handleProgress(20);
    const serverId = JSON.parse(localStorage.getItem("configId"));

    try {
      props.handleProgress(50);
      await axios
        .get(`${url}lims/ready-manifests/${manifestsId}/${serverId}`, {
          headers: { Authorization: `Bearer ${token}` },
        })
        .then((resp) => {
          props.handleProgress(70);

          if (resp) {
            console.log("re sending manifest", resp);
            props.handleProgress(100);
          }

          toast.success("Sample manifest sent successfully to PCR Lab.", {
            position: toast.POSITION.TOP_RIGHT,
          });
        })
        .catch((err) => {
          props.handleProgress(10);

          toast.success("Server currently down!!! Try sending manifest later", {
            position: toast.POSITION.TOP_CENTER,
          });
          props.handleProgress(0);
          props.handleOpen();
        });
    } catch (err) {
      props.handleProgress(10);
      toast.error("Error encountered while sending manifest", {
        position: toast.POSITION.TOP_RIGHT,
      });
      props.handleOpen();
    }
  };

  return (
    <div>
      <Card>
        <CardBody>
          <Modal
            isOpen={props.modalstatus}
            toggle={props.togglestatus}
            className={props.className}
            size="lg"
          >
            <Form onSubmit={saveSample}>
              <ModalHeader toggle={props.togglestatus}></ModalHeader>
              <ModalBody>
                {props.failed ? (
                  ""
                ) : (
                  <>
                    <Row>
                      <Col>
                        <FormGroup>
                          <Label for="configName" className={classes.label}>
                            Which PCR Server are you sending to?
                          </Label>
                          <Input
                            type="select"
                            name="config"
                            id="config"
                            className={classes.input}
                            onChange={handleChange}
                          >
                            <option hidden>Select Server</option>
                            {
                              <option key={1} value={logins.id}>
                                {logins.configName}
                              </option>
                            }
                          </Input>
                        </FormGroup>
                      </Col>
                      <Col></Col>
                    </Row>

                    <MatButton
                      variant="contained"
                      color="secondary"
                      startIcon={<SendIcon />}
                      type="submit"
                      onClick={sendManifest}
                      disabled={saved ? false : true}
                    >
                      Send
                    </MatButton>
                  </>
                )}

                {!props.failed ? (
                  ""
                ) : (
                  <>
                    <MatButton
                      type="submit"
                      variant="contained"
                      color="primary"
                      className={classes.button}
                      startIcon={<SaveIcon />}
                      disabled={loading}
                      onClick={saveSample}
                    >
                      Save
                    </MatButton>{" "}
                    <MatButton
                      variant="contained"
                      color="secondary"
                      startIcon={<SendIcon />}
                      type="submit"
                      onClick={resendManifest}
                    >
                      Re-send
                    </MatButton>{" "}
                    <MatButton
                      variant="contained"
                      color="default"
                      onClick={props.togglestatus}
                      className={classes.button}
                      startIcon={<CancelIcon />}
                    >
                      Cancel
                    </MatButton>
                  </>
                )}
              </ModalBody>
            </Form>
          </Modal>
        </CardBody>
      </Card>
    </div>
  );
};

export default ConfigModal;
