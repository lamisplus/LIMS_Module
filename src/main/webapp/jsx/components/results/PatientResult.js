import React from "react";
import { Badge, Card, CardBody, Table } from "reactstrap";
import { Row } from "react-bootstrap";
import "../../../css/result.css";
import { logo } from "../SampleCollection/pcr";

const today = new Date().toLocaleDateString("en-us", {
  weekday: "long",
  year: "numeric",
  month: "short",
  day: "numeric",
});

const transferStatus = (status) => {
  switch (parseInt(status)) {
    case 1:
      return <p color="secondary">Not Transferred</p>;
    case 2:
      return <p color="primary">Received</p>;
    case 3:
      return <p color="info">In-Progress</p>;
    case 4:
      return <p color="warning">Tested</p>;
    default:
      return <p color="dark">None</p>;
  }
};

class PatientResult extends React.Component {
  render() {
    const { samples } = this.props;
    //    console.log(samples);
    if (!samples)
      return <div className="loading-message">Loading patient result...</div>;

    return (
      <div className="result-container">
        <Card className="report-card">
          <CardBody>
            <header className="report-header">
              <div className="header-text">
                <h2 className="report-title">NISRN Viral Load Result</h2>
                <h5 className="report-titled">{samples.receivingLabName}</h5>
                <p className="report-subtitle">
                  National Integrated Sample Referral Network
                </p>
              </div>
              <div className="report-logo">
                <img src={logo} alt="NISRN Logo" />
              </div>
            </header>

            <section className="section">
              <Table className="report-table">
                <tbody>
                  <tr>
                    <th>Manifest ID</th>
                    <td>{samples.manifestID}</td>
                    <th>Surname</th>
                    <td>{samples.surName}</td>
                    <th>First Name</th>
                    <td>{samples.firstName}</td>
                  </tr>
                  <tr>
                    <th>Gender</th>
                    <td>{samples.sex}</td>
                    <th>Age</th>
                    <td>{samples.age}</td>
                  </tr>
                  <tr>
                    <th>Unique Client No.</th>
                    <td>{samples.patientID?.[2]?.idNumber || ""}</td>
                    <th>Hospital Number</th>
                    <td>{samples.patientID?.[0]?.idNumber || ""}</td>
                    <th>Test Number</th>
                    <td>{samples.patientID?.[1]?.idNumber || ""}</td>
                  </tr>
                  <tr>
                    <th>Facility</th>
                    <td>{samples.sendingFacilityName}</td>
                  </tr>
                </tbody>
              </Table>
            </section>

            <section className="section">
              <Table className="report-table">
                <tbody>
                  <tr>
                    <th>Sample Collected By</th>
                    <td>{samples.sampleCollectedBy}</td>
                    <th>Collection Date/Time</th>
                    <td>{samples.sampleCollectionDate}</td>
                  </tr>
                  <tr>
                    <th>Sample ID</th>
                    <td>{samples.sampleID}</td>
                    <th>Sample Type</th>
                    <td>{samples.sampleType}</td>
                  </tr>
                  <tr>
                    <th>Order Date</th>
                    <td>{samples.sampleOrderDate}</td>
                    <th>Date Received at PCR</th>
                    <td>{samples.visitDate}</td>
                  </tr>
                </tbody>
              </Table>
            </section>

            <section className="section">
              <Table className="report-table">
                <tbody>
                  <tr>
                    <th>Test Type</th>
                    <td>Viral Load</td>
                    <th>VL Indication</th>
                    <td>{samples.indicationVLTest}</td>
                  </tr>
                  <tr>
                    <th>Receiving Lab</th>
                    <td>{samples.receivingLabName}</td>
                    <th>Receiving Lab Number</th>
                    <td>{samples.receivingLabID}</td>
                  </tr>
                </tbody>
              </Table>
            </section>

            <section className="section">
              <Table className="report-table">
                <tbody>
                  <tr>
                    <td>
                      <strong>Transferred Out Date: </strong>
                      <br />
                      {samples.dateTransferredOut || "None"}
                    </td>
                    <td>
                      <strong>Transfer Status: </strong>
                      <br />
                      {transferStatus(samples.transferStatus)}
                    </td>
                    <td>
                      <strong>Result Dispatch Date: </strong>
                      <br />
                      {samples.dateResultDispatched}
                    </td>
                    <td>
                      <strong>PCR Sample No: </strong>
                      <br />
                      {samples.pcrLabSampleNumber}
                    </td>
                    <td>
                      <strong>Test Result: </strong>
                      <br />
                      {samples.testResult}
                    </td>
                  </tr>
                </tbody>
              </Table>
            </section>

            <section className="section">
              <Table className="report-table">
                <tbody>
                  <tr>
                    <th>Ordered by</th>
                    <td>{samples.sampleOrderedBy}</td>
                    <th>Ordered Date</th>
                    <td>{samples.sampleOrderDate}</td>
                  </tr>
                  <tr>
                    <th>Tested by</th>
                    <td>{samples.testedBy}</td>
                    <th>Tested Date</th>
                    <td>{samples.dateResultDispatched}</td>
                  </tr>
                  <tr>
                    <th>Assayed by</th>
                    <td>{samples.approvedBy}</td>
                    <th>Assayed Date</th>
                    <td>{samples.assayDate}</td>
                  </tr>
                  <tr>
                    <th>Approved by</th>
                    <td>{samples.approvedBy}</td>
                    <th>Approval Date</th>
                    <td>{samples.approvalDate}</td>
                  </tr>
                  <tr>
                    <th>Reviewed by</th>
                    <td>______________________</td>
                    <th>Signature</th>
                    <td>______________________</td>
                  </tr>
                </tbody>
              </Table>
            </section>

            <footer className="report-footer">
              <p>LAMISPlus 2.0 | {today}</p>
            </footer>
          </CardBody>
        </Card>
      </div>
    );
  }
}

export default PatientResult;
