import React, {
  useState,
} from "react";
import { Link, useHistory } from "react-router-dom";
import { Row, Col, Card } from "react-bootstrap";
import MatButton from "@material-ui/core/Button";
import HomeIcon from "@mui/icons-material/Home";
import SaveIcon from "@material-ui/icons/Save";
import Alert from "react-bootstrap/Alert";
import { format } from "date-fns";
import { pcr_lab } from "../SampleCollection/pcr";

import {
  Form,
  FormGroup,
  Input,
  Label,
} from "reactstrap";

import "../SampleCollection/sample.css";
import AddIcon from "@mui/icons-material/Add";
import DeleteIcon from "@mui/icons-material/Delete";

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
  arial: {
    border: "1px solid #014d88",
    borderRadius: "0px",
    fontSize: "15px",
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
    fontWeight: "bold",
  },
}));

const AddResult = (props) => {
  let history = useHistory();
  const manifestObj =
    history.location && history.location.state
      ? history.location.state.manifestObj
      : {};
  const permissions =
    history.location && history.location.state
      ? history.location.state.permissions
      : [];
  const sampleIDs = [];
  const [pcrLabCode, setPcrLabCode] = useState({ name: "", labNo: "" });
  manifestObj.sampleInformation.forEach((e) => {
    sampleIDs.push(e);
  });

  //console.log("maniObj",manifestObj)
  //console.log("permissions",permissions)
  const classes = useStyles();

  const [initialValue, SetInitialValue] = useState(0);
  const [tests, setTests] = useState(false);
  const [transferredOut, setTransferredOut] = useState(false);
  const [reasons, setReasons] = useState(false);

  const [inputFields, setInputFields] = useState([
    {
      manifestRecordID: manifestObj.id,
      dateResultDispatched: "",
      dateSampleReceivedAtPcrLab: "",
      testResult: "",
      resultDate: "",
      pcrLabSampleNumber: "",
      approvalDate: "",
      assayDate: "",
      sampleTestable: "",
      sampleStatus: "",
      sampleID: sampleIDs[initialValue],
      uuid: "",
      visitDate: format(new Date(), "yyyy-MM-dd"),
      transferStatus: "",
      testedBy: "",
      approvedBy: "",
      dateTransferredOut: "",
      reasonNotTested: "",
      otherRejectionReason: "",
      sendingPCRLabID: "",
      sendingPCRLabName: "",
    },
  ]);

  const handleChange = (i, event) => {
    let data = [...inputFields];
    const { name, value } = event.target;

    //console.log(name, value)

    if (name === "sendingPCRLabName") {
      checkPCRLab(value);
      console.log(pcrLabCode.labNo);
    }

    if (
      (name === "transferStatus" && value === "2") ||
      (name === "transferStatus" && value === "3") ||
      (name === "transferStatus" && value === "4")
    ) {
      setTests(true);
      setTransferredOut(true);
    }

    if (name === "reasonNotTested" && value === "7") {
      setReasons(true);
    }

    data[i].manifestRecordID = manifestObj.id;
    data[i][name] = value;
    data[i].uuid = "";
    data[i].visitDate = format(new Date(), "yyyy-MM-dd");

    setInputFields(data);
    //console.log("inputs",inputFields)
  };

  const checkPCRLab = (name) => {
    pcr_lab.forEach((val) => {
      if (val.name === name) {
        setPcrLabCode({ name: val.name, labNo: val.labNo });
      }
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    console.log("inputFields", inputFields);
    try {
      //console.log(inputFields);

      await axios
        .post(`${url}lims/results`, inputFields, {
          headers: { Authorization: `Bearer ${token}` },
        })
        .then((resp) => {
          console.log("results", resp);

          toast.success("PCR Sample results added successfully!!", {
            position: toast.POSITION.TOP_RIGHT,
          });
        });
      history.push("/");
    } catch (e) {
      toast.error("An error occurred while adding PCR Sample results", {
        position: toast.POSITION.TOP_RIGHT,
      });
    }
  };

  const addField = (e) => {
    e.preventDefault();
    SetInitialValue(initialValue + 1);

    //        console.log(initialValue)
    //        console.log(sampleIDs)
    //        console.log(sampleIDs[initialValue])

    if (initialValue === 0) {
      toast.success("click the Add More button to add more fields...", {
        position: toast.POSITION.TOP_RIGHT,
      });
    }

    if (initialValue > 0) {
      let newField = {
        testResult: "",
        resultDate: "",
        pcrLabSampleNumber: "",
        approvalDate: "",
        assayDate: "",
        sampleTestable: "",
        sampleStatus: "",
        sampleID: sampleIDs[initialValue],
      };

      if (initialValue < sampleIDs.length) {
        setInputFields([...inputFields, newField]);
      } else {
        toast.error("Total Number of samples reached for this manifest", {
          position: toast.POSITION.TOP_RIGHT,
        });
      }
    }
  };

  const removeField = (index, e) => {
    e.preventDefault();
    SetInitialValue(initialValue - 1);
    let data = [...inputFields];
    data.splice(index, 1);
    setInputFields(data);
  };

  return (
    <div>
      <Card>
        <Card.Body>
          <p style={{ textAlign: "right" }}>
            <Link color="inherit" to={{ pathname: "/" }}>
              <MatButton
                variant="contained"
                color="primary"
                className={classes.button}
                startIcon={<HomeIcon />}
              >
                back Home
              </MatButton>
            </Link>
          </p>
          <hr />
          <Form>
            <Alert
              style={{
                width: "100%",
                fontSize: "16px",
                backgroundColor: "#014d88",
                color: "#fff",
                textAlign: "center",
              }}
            >
              <Alert.Heading>Basic Manifest Information</Alert.Heading>
            </Alert>
            <Row>
              <Col>
                {" "}
                <FormGroup>
                  <Label for="manifestID" className={classes.label}>
                    Manifest Id
                  </Label>

                  <Input
                    type="text"
                    name="manifestID"
                    id="manifestID"
                    placeholder="manifest ID"
                    className={classes.input}
                    onChange={handleChange}
                    value={manifestObj.manifestID}
                    disabled
                  />
                </FormGroup>
              </Col>
              <Col>
                <FormGroup>
                  <Label for="testType" className={classes.label}>
                    Test Type
                  </Label>

                  <Input
                    type="text"
                    name="testType"
                    id="testType"
                    placeholder="Test Type"
                    className={classes.input}
                    onChange={handleChange}
                    value="Viral Load  "
                  />
                </FormGroup>
              </Col>
              <Col></Col>
              <Col></Col>
            </Row>
            <Row>
              <Col>
                <FormGroup>
                  <Label for="sendingPCRLabName" className={classes.label}>
                    Facility
                  </Label>

                  <Input
                    type="text"
                    name="sendingPCRLabName"
                    id="sendingPCRLabName"
                    placeholder="Sending PCR LabName"
                    className={classes.input}
                    onChange={handleChange}
                    value={manifestObj.sendingFacilityName}
                    disabled
                  />
                </FormGroup>
              </Col>
              <Col>
                <FormGroup>
                  <Label for="sendingPCRLabID" className={classes.label}>
                    Facility ID
                  </Label>

                  <Input
                    type="text"
                    name="sendingPCRLabID"
                    id="sendingPCRLabID"
                    placeholder="Sending PCR Lab ID"
                    className={classes.input}
                    onChange={handleChange}
                    value={manifestObj.sendingFacilityID}
                    disabled
                  />
                </FormGroup>
              </Col>
              <Col>
                {" "}
                <FormGroup>
                  <Label for="receivingFacilityName" className={classes.label}>
                    Receiving Facility
                  </Label>

                  <Input
                    type="text"
                    name="receivingFacilityName"
                    id="receivingFacilityName"
                    placeholder="Receiving Facility Name"
                    className={classes.input}
                    onChange={handleChange}
                    value={manifestObj.receivingLabName}
                    disabled
                  />
                </FormGroup>
              </Col>
              <Col>
                <FormGroup>
                  <Label for="receivingFacilityID" className={classes.label}>
                    Receiving Facility ID
                  </Label>

                  <Input
                    type="text"
                    name="receivingFacilityID"
                    id="receivingFacilityID"
                    placeholder="Receiving Facility ID"
                    className={classes.input}
                    onChange={handleChange}
                    value={manifestObj.receivingLabID}
                    disabled
                  />
                </FormGroup>
              </Col>
            </Row>
            <br />
            <Alert
              style={{
                width: "100%",
                fontSize: "16px",
                backgroundColor: "#992E62",
                color: "#fff",
                textAlign: "center",
              }}
            >
              <Alert.Heading>PCR Sample Details</Alert.Heading>
            </Alert>
            {inputFields &&
              inputFields.map((x) =>
                sampleIDs
                  .filter((key) => key.sampleID === x.sampleID)
                  .map((x) => (
                    <Alert
                      style={{
                        width: "100%",
                        fontSize: "16px",
                        backgroundColor: "#014d88",
                        color: "#fff",
                        textAlign: "center",
                      }}
                    >
                      <p style={{ marginTop: ".7rem" }}>
                        Name:{" "}
                        <span style={{ fontWeight: "bolder" }}>
                          {x.firstName + " " + x.surName + " "}
                        </span>
                        &nbsp;&nbsp;&nbsp;&nbsp; Patient ID::
                        <span style={{ fontWeight: "bolder" }}>
                          {" "}
                          {x.patientID[0].idNumber}
                        </span>
                        &nbsp;&nbsp;&nbsp;&nbsp;Sample type:
                        <span style={{ fontWeight: "bolder" }}>
                          {" "}
                          {x.sampleType}
                        </span>
                        &nbsp;&nbsp;&nbsp;&nbsp; Date collected :
                        <span style={{ fontWeight: "bolder" }}>
                          {" "}
                          {x.sampleCollectionDate}
                        </span>
                        &nbsp;&nbsp;&nbsp;&nbsp; Sample collected By:
                        <span style={{ fontWeight: "bolder" }}>
                          {" "}
                          {x.sampleCollectedBy}
                        </span>
                      </p>
                    </Alert>
                  ))
              )}
            {manifestObj.sampleInformation.length > 0 &&
              inputFields.map((data, i) => (
                <>
                  <Row>
                    <Col>
                      <FormGroup>
                        <Label for="sampleID" className={classes.label}>
                          Sample ID <span style={{ color: "red" }}> *</span>
                        </Label>
                        <select
                          className="form-control"
                          name="sampleID"
                          id="sampleID"
                          style={{
                            border: "1px solid #014d88",
                            borderRadius: "0px",
                            fontSize: "14px",
                            color: "#000",
                          }}
                          onChange={(e) => handleChange(i, e)}
                        >
                          <option hidden>Select Sample Id</option>
                          {sampleIDs &&
                            sampleIDs.map((sample, i) => (
                              <option key={i} value={sample.sampleID}>
                                {sample.sampleID}
                              </option>
                            ))}
                        </select>
                      </FormGroup>
                    </Col>
                    <Col>
                      <FormGroup>
                        <Label for="sampleTestable" className={classes.label}>
                          Sample Testable <span style={{ color: "red" }}> *</span>
                        </Label>
                        <select
                          className="form-control"
                          name="sampleTestable"
                          id="sampleTestable"
                          style={{
                            border: "1px solid #014d88",
                            borderRadius: "0px",
                            fontSize: "14px",
                            color: "#000",
                          }}
                          onChange={(e) => handleChange(i, e)}
                        >
                          <option hidden>Is Sample Testable ?</option>
                          <option value="true">True</option>
                          <option value="false">False</option>
                        </select>
                      </FormGroup>
                    </Col>

                    <Col>
                      <FormGroup>
                        <Label for="sampleStatus" className={classes.label}>
                          Sample Status <span style={{ color: "red" }}> *</span>
                        </Label>
                        <select
                          className="form-control"
                          name="sampleStatus"
                          id="sampleStatus"
                          style={{
                            border: "1px solid #014d88",
                            borderRadius: "0px",
                            fontSize: "14px",
                            color: "#000",
                          }}
                          onChange={(e) => handleChange(i, e)}
                        >
                          <option hidden>Select Sample status</option>
                          <option value="1">Completed</option>
                          <option value="2">Rejected</option>
                          <option value="3">In-Progress</option>
                          <option value="4">Re-run</option>
                        </select>
                      </FormGroup>
                    </Col>

                    <Col>
                      {" "}
                      <FormGroup>
                        <Label for="assayDate" className={classes.label}>
                          Assay Date <span style={{ color: "red" }}> *</span>
                        </Label>

                        <Input
                          type="date"
                          name="assayDate"
                          id="assayDate"
                          min={manifestObj.createDate.slice(0, 10)}
                          max={new Date().toISOString().slice(0, 10)}
                          //min={new Date(datasample.dateSampleVerified)}
                          placeholder="Assay Date"
                          className={classes.input}
                          onChange={(e) => handleChange(i, e)}
                        />
                      </FormGroup>
                    </Col>
                  </Row>
                  <Row>
                    <Col>
                      <FormGroup>
                        <Label for="transferStatus" className={classes.label}>
                          Transfer Status
                        </Label>
                        <select
                          className="form-control"
                          name="transferStatus"
                          id="transferStatus"
                          style={{
                            border: "1px solid #014d88",
                            borderRadius: "0px",
                            fontSize: "14px",
                            color: "#000",
                          }}
                          onChange={(e) => handleChange(i, e)}
                          value={inputFields.transferStatus}
                        >
                          <option hidden>Select transfer status</option>
                          <option value="1">Not Transfered</option>
                          <option value="2">Received</option>
                          <option value="3">In Process</option>
                          <option value="4">Tested</option>
                        </select>
                      </FormGroup>
                    </Col>

                    <Col>
                      <FormGroup>
                        <Label for="reasonNotTested" className={classes.label}>
                          Reason Not Tested
                        </Label>
                        <select
                          className="form-control"
                          name="reasonNotTested"
                          id="reasonNotTested"
                          style={{
                            border: "1px solid #014d88",
                            borderRadius: "0px",
                            fontSize: "14px",
                            color: "#000",
                          }}
                          onChange={(e) => handleChange(i, e)}
                          value={inputFields.reasonNotTested}
                        >
                          <option hidden>
                            What is the reasons not tested?
                          </option>
                          <option value="1">Testable</option>
                          <option value="2">Technical Problems</option>
                          <option value="3">Labeled Improperly</option>
                          <option value="4">Insufficient Blood</option>
                          <option value="5">Layered or clotted</option>
                          <option value="6">Improper Packaging</option>
                          <option value="7">Other Reasons</option>
                        </select>
                      </FormGroup>
                    </Col>

                    <Col>
                      <FormGroup>
                        <Label for="approvedBy" className={classes.label}>
                          Approved By <span style={{ color: "red" }}> *</span>
                        </Label>

                        <Input
                          type="text"
                          name="approvedBy"
                          id="approvedBy"
                          placeholder="approvedBy"
                          className={classes.input}
                          onChange={(e) => handleChange(i, e)}
                          value={inputFields.approvedBy}
                        />
                      </FormGroup>
                    </Col>

                    <Col>
                      <FormGroup>
                        <Label for="testedBy" className={classes.label}>
                          Test By <span style={{ color: "red" }}> *</span>
                        </Label>

                        <Input
                          type="text"
                          name="testedBy"
                          id="testedBy"
                          placeholder="Test By"
                          className={classes.input}
                          onChange={(e) => handleChange(i, e)}
                          value={inputFields.testedBy}
                        />
                      </FormGroup>
                    </Col>

                    {transferredOut === true ? (
                      <Col>
                        <FormGroup>
                          <Label
                            for="dateTransferredOut"
                            className={classes.label}
                          >
                            Date Transferred Out
                          </Label>

                          <Input
                            type="date"
                            name="dateTransferredOut"
                            id="dateTransferredOut"
                            placeholder="Date Transferred Out"
                            max={new Date().toISOString().slice(0, 10)}
                            className={classes.input}
                            onChange={(e) => handleChange(i, e)}
                            value={inputFields.dateTransferredOut}
                          />
                        </FormGroup>
                      </Col>
                    ) : (
                      " "
                    )}
                    {reasons === true ? (
                      <Col>
                        <FormGroup>
                          <Label
                            for="otherRejectionReason"
                            className={classes.label}
                          >
                            Other Rejection Reason
                          </Label>
                          <Input
                            type="text"
                            name="otherRejectionReason"
                            id="otherRejectionReason"
                            placeholder="Other Rejection Reason"
                            className={classes.input}
                            onChange={(e) => handleChange(i, e)}
                            value={inputFields.otherRejectionReason}
                          />
                        </FormGroup>
                      </Col>
                    ) : (
                      " "
                    )}
                  </Row>
                  <Row>
                    <Col>
                      <FormGroup>
                        <Label
                          for="dateSampleReceivedAtPcrLab"
                          className={classes.label}
                        >
                          Date sample at PCR Lab <span style={{ color: "red" }}> *</span>
                        </Label>

                        <Input
                          type="date"
                          name="dateSampleReceivedAtPcrLab"
                          id="dateSampleReceivedAtPcrLab"
                          placeholder="result Date"
                          min={manifestObj.createDate.slice(0, 10)}
                          max={new Date().toISOString().slice(0, 10)}
                          className={classes.input}
                          onChange={(e) => handleChange(i, e)}
                        />
                      </FormGroup>
                    </Col>
                    <Col>
                      <FormGroup>
                        <Label
                          for="dateResultDispatched"
                          className={classes.label}
                        >
                          Date Result Dispatched <span style={{ color: "red" }}> *</span>
                        </Label>

                        <Input
                          type="date"
                          name="dateResultDispatched"
                          id="dateResultDispatched"
                          placeholder="result Date"
                          min={manifestObj.createDate.slice(0, 10)}
                          max={new Date().toISOString().slice(0, 10)}
                          className={classes.input}
                          onChange={(e) => handleChange(i, e)}
                        />
                      </FormGroup>
                    </Col>
                    <Col>
                      <FormGroup>
                        <Label for="approvalDate" className={classes.label}>
                          Approval Date <span style={{ color: "red" }}> *</span>
                        </Label>

                        <Input
                          type="date"
                          name="approvalDate"
                          id="approvalDate"
                          placeholder="Approval Date"
                          min={manifestObj.createDate.slice(0, 10)}
                          max={new Date().toISOString().slice(0, 10)}
                          className={classes.input}
                          onChange={(e) => handleChange(i, e)}
                        />
                      </FormGroup>
                    </Col>
                    <Col>
                      <FormGroup>
                        <Label
                          for="pcrLabSampleNumber"
                          className={classes.label}
                        >
                          Pcr Lab Sample No <span style={{ color: "red" }}> *</span>
                        </Label>

                        <Input
                          type="text"
                          name="pcrLabSampleNumber"
                          id="pcrLabSampleNumber"
                          placeholder="Pcr Lab Sample Number"
                          className={classes.input}
                          onChange={(e) => handleChange(i, e)}
                        />
                      </FormGroup>
                    </Col>
                  </Row>
                  <Row>
                    <Col>
                      <FormGroup>
                        <Label for="resultDate" className={classes.label}>
                          Result Date <span style={{ color: "red" }}> *</span>
                        </Label>

                        <Input
                          type="date"
                          name="resultDate"
                          id="resultDate"
                          placeholder="result Date"
                          min={manifestObj.createDate.slice(0, 10)}
                          max={new Date().toISOString().slice(0, 10)}
                          className={classes.input}
                          onChange={(e) => handleChange(i, e)}
                        />
                      </FormGroup>
                    </Col>
                    <Col>
                      <FormGroup>
                        <Label for="testResult" className={classes.label}>
                          Test result <span style={{ color: "red" }}> *</span>
                        </Label>

                        <Input
                          type="text"
                          name="testResult"
                          id="testResult"
                          placeholder="Test result"
                          className={classes.input}
                          onChange={(e) => handleChange(i, e)}
                        />
                      </FormGroup>
                    </Col>
                    {tests === true ? (
                      <>
                        <Col>
                          <FormGroup>
                            <Label
                              for="sendingPCRLabName"
                              className={classes.label}
                            >
                              Transferred PCR Lab Name
                            </Label>

                            <select
                              className="form-control"
                              style={{
                                border: "1px solid #014d88",
                                borderRadius: "0px",
                                fontSize: "14px",
                                color: "#000",
                              }}
                              name="sendingPCRLabName"
                              value={pcrLabCode.name}
                              id="sendingPCRLabName"
                              onChange={(e) => handleChange(i, e)}
                            >
                              <option>Select PCR Lab</option>
                              {pcr_lab.map((value, i) => (
                                <option key={i} value={value.name}>
                                  {value.name}
                                </option>
                              ))}
                            </select>
                          </FormGroup>
                        </Col>
                        <Col>
                          <FormGroup>
                            <Label
                              for="sendingPCRLabID"
                              className={classes.label}
                            >
                              Transferred PCR Lab ID
                            </Label>
                            &nbsp;&nbsp;
                            <span>
                              Confirm PCR Id <b>{pcrLabCode.labNo}</b>
                            </span>
                            <Input
                              type="text"
                              name="sendingPCRLabID"
                              id="sendingPCRLabID"
                              placeholder="Transferred PCR Lab ID"
                              value={inputFields.sendingPCRLabID}
                              className={classes.input}
                              onChange={(e) => handleChange(i, e)}
                            />
                          </FormGroup>
                        </Col>
                      </>
                    ) : (
                      <>
                        <Col></Col>
                        <Col></Col>
                      </>
                    )}
                  </Row>

                  <Row>
                    <Col style={{ textAlign: "right" }}>
                      <Button
                        variant="contained"
                        color="error"
                        startIcon={<DeleteIcon />}
                        onClick={(e) => removeField(i, e)}
                      >
                        Remove PCR Sample
                      </Button>
                    </Col>
                  </Row>
                  <hr />
                </>
              ))}
            {permissions.includes("all_permission") ? (
              <Button
                variant="contained"
                color="secondary"
                startIcon={<AddIcon />}
                onClick={addField}
              >
                Add More
              </Button>
            ) : (
              " "
            )}{" "}
            {permissions.includes("all_permission") ? (
              <Button
                variant="contained"
                color="primary"
                type="submit"
                startIcon={<SaveIcon />}
                onClick={handleSubmit}
              >
                Save Result
              </Button>
            ) : (
              " "
            )}
          </Form>
        </Card.Body>
      </Card>
    </div>
  );
};

export default AddResult;
