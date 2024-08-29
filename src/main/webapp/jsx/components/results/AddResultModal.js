import React, { useState, useEffect } from "react";
import {
  Modal,
  ModalHeader,
  ModalBody,
  Form,
  Row,
  Alert,
  Col,
  Input,
  FormGroup,
  Label,
  Card,
  CardBody,
} from "reactstrap";
import axios from "axios";
import { format } from "date-fns";
import MatButton from "@material-ui/core/Button";
import { makeStyles } from "@material-ui/core/styles";
import SaveIcon from "@material-ui/icons/Save";
import CancelIcon from "@material-ui/icons/Cancel";
import "react-toastify/dist/ReactToastify.css";
import "react-widgets/styles.css";
import { token, url } from "../../../api";
import { toast } from "react-toastify";
import { pcr_lab } from "../SampleCollection/pcr";

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
    border: "2px solid #014d88",
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
  modalStyle1: {
    position: "absolute",
    overflow: "scroll",
    height: "100%",
  },
}));

const AddResultModal = (props) => {
  const classes = useStyles();
  const { manifestObj, reload } = props;
  //console.log("main", manifestObj)

  const sampleIDs = [];
  manifestObj.sampleInformation.forEach((e) => {
    sampleIDs.push(e);
  });

  const [pcrLabCode, setPcrLabCode] = useState({ name: "", labNo: "" });

  const [loading, setLoading] = useState(false);
  const [visible, setVisible] = useState(true);
  const onDismiss = () => setVisible(false);

  const [tests, setTests] = useState(false);
  const [transferredOut, setTransferredOut] = useState(false);
  const [reasons, setReasons] = useState(false);
  const [reasonsNot, setReasonsNot] = useState(false);
  const [transferredStatus, setTransferredStatus] = useState(false);
  const [inputFields, setInputFields] = useState({
    manifestRecordID: manifestObj.id,
    //id: 0,
    dateResultDispatched: "",
    dateSampleReceivedAtPcrLab: "",
    testResult: "",
    resultDate: "",
    pcrLabSampleNumber: "",
    approvalDate: "",
    assayDate: "",
    sampleTestable: "",
    sampleStatus: "",
    sampleID: "",
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
  });

  useEffect(() => {}, []);

  const checkPCRLab = (name) => {
    pcr_lab.forEach((val) => {
      if (val.name === name) {
        setPcrLabCode({ name: val.name, labNo: val.labNo });
      }
    });
  };

  const handleChange = (event) => {
    event.preventDefault();

    const { name, value } = event.target;
    console.log(name, value);

    if (name === "sendingPCRLabName") {
      checkPCRLab(value);
      console.log(pcrLabCode.labNo);
    }

    if (name === "sampleStatus" && value === "2") {
      setReasonsNot(true);
      setTransferredStatus(false);
    }

    if (name === "sampleStatus" && value === "5") {
      setTransferredStatus(true);
      setReasonsNot(false);
    }

    if (
      (name === "transferStatus" && value === "2") ||
      (name === "transferStatus" && value === "3") ||
      (name === "transferStatus" && value === "4")
    ) {
      setTests(true);
      setTransferredOut(true);
    } else if (name === "transferStatus" && value === "1") {
      setTests(false);
      setTransferredOut(false);
    }

    if (name === "reasonNotTested" && value === "7") {
      setReasons(true);
    }

    setInputFields({ ...inputFields, [name]: value });
  };

  const saveSample = async (e) => {
    e.preventDefault();

    try {
      console.log(inputFields);

      await axios
        .post(`${url}lims/results`, [inputFields], {
          headers: { Authorization: `Bearer ${token}` },
        })
        .then((resp) => {
          console.log("results", resp);

          toast.success("PCR Sample results added successfully!!", {
            position: toast.POSITION.TOP_RIGHT,
          });

          setInputFields({
            dateResultDispatched: "",
            dateSampleReceivedAtPcrLab: "",
            testResult: "",
            resultDate: "",
            pcrLabSampleNumber: "",
            approvalDate: "",
            assayDate: "",
            sampleTestable: "",
            sampleStatus: "",
            sampleID: "",
            uuid: "",
            visitDate: format(new Date(), "yyyy-MM-dd"),
          });
        });
      //history.push("/");
      props.togglestatus();
      reload();
    } catch (e) {
      toast.error("An error occurred while adding PCR Sample results", {
        position: toast.POSITION.TOP_RIGHT,
      });
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
              <ModalHeader toggle={props.togglestatus}>
                {sampleIDs
                  .filter((key) => key.sampleID === inputFields.sampleID)
                  .map((x) => (
                    <Alert
                      color="primary"
                      style={{ color: "#000", fontWeight: "bolder" }}
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
                  ))}
              </ModalHeader>

              <ModalBody>
                <Row>
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
                        min={manifestObj.createDate.slice(0, 10)}
                        max={new Date().toISOString().slice(0, 10)}
                        className={classes.input}
                        onChange={handleChange}
                        value={inputFields.dateResultDispatched}
                      />
                    </FormGroup>
                  </Col>
                  <Col>
                    <FormGroup>
                      <Label
                        for="dateSampleReceivedAtPcrLab"
                        className={classes.label}
                      >
                        Date Sample Received at PCR Lab <span style={{ color: "red" }}> *</span>
                      </Label>

                      <Input
                        type="date"
                        name="dateSampleReceivedAtPcrLab"
                        id="dateSampleReceivedAtPcrLab"
                        min={manifestObj.createDate.slice(0, 10)}
                        max={new Date().toISOString().slice(0, 10)}
                        className={classes.input}
                        onChange={handleChange}
                        value={inputFields.dateSampleReceivedAtPcrLab}
                      />
                    </FormGroup>
                  </Col>
                </Row>
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
                        onChange={(e) => handleChange(e)}
                        value={inputFields.sampleID}
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
                      <Label for="surName" className={classes.label}>
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
                        onChange={(e) => handleChange(e)}
                        value={inputFields.sampleTestable}
                      >
                        <option hidden>Is Sample Testable ?</option>
                        <option value="true">True</option>
                        <option value="false">False</option>
                      </select>
                    </FormGroup>
                  </Col>
                </Row>

                <Row>
                  <Col>
                    <FormGroup>
                      <Label for="approvedBy" className={classes.label}>
                        Approved By*
                      </Label>

                      <Input
                        type="text"
                        name="approvedBy"
                        id="approvedBy"
                        placeholder="approvedBy"
                        className={classes.input}
                        onChange={handleChange}
                        value={inputFields.approvedBy}
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
                        onChange={handleChange}
                        value={inputFields.approvalDate}
                      />
                    </FormGroup>
                  </Col>
                </Row>
                <Row>
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
                        onChange={(e) => handleChange(e)}
                        value={inputFields.sampleStatus}
                      >
                        <option hidden>Select Sample status</option>
                        <option value="1">Completed</option>
                        <option value="2">Rejected</option>
                        <option value="3">In-Progress</option>
                        <option value="4">Re-run</option>
                        <option value="5">Transferred</option>
                      </select>
                    </FormGroup>
                  </Col>

                  <Col>
                    <FormGroup>
                      <Label for="pcrLabSampleNumber" className={classes.label}>
                        Pcr Lab Sample No <span style={{ color: "red" }}> *</span>
                      </Label>

                      <Input
                        type="text"
                        name="pcrLabSampleNumber"
                        id="pcrLabSampleNumber"
                        placeholder="Pcr Lab Sample Number"
                        className={classes.input}
                        onChange={handleChange}
                        value={inputFields.pcrLabSampleNumber}
                      />
                    </FormGroup>
                  </Col>
                </Row>

                <Row>
                  {transferredStatus ? (
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
                          onChange={(e) => handleChange(e)}
                          value={inputFields.transferStatus}
                        >
                          <option hidden>Select transfer status</option>
                          <option value="1">Not Transferred</option>
                          <option value="2">Received</option>
                          <option value="3">In Process</option>
                          <option value="4">Tested</option>
                        </select>
                      </FormGroup>
                    </Col>
                  ) : (
                    ""
                  )}

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
                          min={manifestObj.createDate.slice(0, 10)}
                          max={new Date().toISOString().slice(0, 10)}
                          className={classes.input}
                          onChange={handleChange}
                          value={inputFields.dateTransferredOut}
                        />
                      </FormGroup>
                    </Col>
                  ) : (
                    " "
                  )}

                  {reasonsNot ? (
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
                          onChange={(e) => handleChange(e)}
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
                  ) : (
                    ""
                  )}
                </Row>

                <Row>
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
                          onChange={handleChange}
                          value={inputFields.otherRejectionReason}
                        />
                      </FormGroup>
                    </Col>
                  ) : (
                    " "
                  )}
                </Row>

                {tests === true ? (
                  <Row>
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
                          onChange={(e) => handleChange(e)}
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
                        <Label for="sendingPCRLabID" className={classes.label}>
                          Transferred PCR Lab ID
                        </Label>
                        &nbsp;&nbsp;
                        <span>
                          <b>
                            {pcrLabCode.labNo
                              ? "Confirm PCR Id " + pcrLabCode.labNo
                              : ""}
                          </b>
                        </span>
                        <Input
                          type="text"
                          name="sendingPCRLabID"
                          id="sendingPCRLabID"
                          placeholder="Transferred PCR Lab ID"
                          value={inputFields.sendingPCRLabID}
                          className={classes.input}
                          onChange={(e) => handleChange(e)}
                        />
                      </FormGroup>
                    </Col>
                  </Row>
                ) : (
                  " "
                )}

                <Row>
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
                        placeholder="Assay Date"
                        min={manifestObj.createDate.slice(0, 10)}
                        max={new Date().toISOString().slice(0, 10)}
                        className={classes.input}
                        onChange={handleChange}
                        value={inputFields.assayDate}
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
                        onChange={handleChange}
                        value={inputFields.testedBy}
                      />
                    </FormGroup>
                  </Col>
                </Row>
                <Row>
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
                        onChange={handleChange}
                        value={inputFields.testResult}
                      />
                    </FormGroup>
                  </Col>
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
                        onChange={handleChange}
                        value={inputFields.resultDate}
                      />
                    </FormGroup>
                  </Col>
                </Row>

                <MatButton
                  type="submit"
                  variant="contained"
                  color="primary"
                  className={classes.button}
                  startIcon={<SaveIcon />}
                  disabled={loading}
                >
                  Save
                </MatButton>

                <MatButton
                  variant="contained"
                  color="default"
                  onClick={props.togglestatus}
                  className={classes.button}
                  startIcon={<CancelIcon />}
                >
                  Cancel
                </MatButton>
              </ModalBody>
            </Form>
          </Modal>
        </CardBody>
      </Card>
    </div>
  );
};

export default AddResultModal;
