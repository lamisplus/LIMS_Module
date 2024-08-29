import React from "react";
import { Link } from "react-router-dom";
import { Badge, Spinner } from "reactstrap";
import { Row } from "react-bootstrap";
import PrintIcon from "@mui/icons-material/Print";

import { Card, CardBody, Table } from "reactstrap";

let today = new Date().toLocaleDateString("en-us", {
  weekday: "long",
  year: "numeric",
  month: "short",
  day: "numeric",
});

const sampleStatus = (e) => {
  //console.log(e)
  if (parseInt(e) === 1) {
    return (
      <p>
        <Badge
          color="#013220"
          style={{ backgroundColor: "#006400", color: "#fff" }}
        >
          Result available
        </Badge>
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
        <Badge color="dark">Pending</Badge>
      </p>
    );
  }
};

class PrintResults extends React.Component {
  render() {
    const { manifestObj, results } = this.props;

    return (
      <Card>
        <CardBody>
          <h3 style={{ textAlign: "center" }}>NISRN SAMPLE RESULTS</h3>
          <hr />
          <Row>
            <Table bordered size="sm" responsive>
              <tbody>
                <tr>
                  <th scope="row">ManifestID:</th>
                  <td>{manifestObj.manifestID}</td>
                  <th scope="row">Facility Name:</th>
                  <td>{manifestObj.sendingFacilityName}</td>
                  <th scope="row">Facility Id:</th>
                  <td>{manifestObj.sendingFacilityID}</td>
                </tr>

                <tr>
                  <th scope="row">Test Type:</th>
                  <td>
                    {
                      <p>
                        <Badge
                          color="#014d88"
                          style={{ backgroundColor: "#014d88", color: "#fff" }}
                        >
                          Viral Load
                        </Badge>
                      </p>
                    }
                  </td>
                  <th scope="row">Receiving Lab Name:</th>
                  <td>{manifestObj.receivingLabName}</td>
                  <th scope="row">Receiving Lab Number:</th>
                  <td>{manifestObj.receivingLabID}</td>
                </tr>
              </tbody>
            </Table>
            <br />
            <Table striped bordered size="sm">
              <tbody>
                <tr style={{ backgroundColor: "#014d88", color: "#fff" }}>
                  <th>Sample ID</th>
                  <th>Approval Date</th>
                  <th>Date Result Dispatched</th>
                  <th>PCR Sample Number</th>
                  <th>Sample Status</th>
                  <th>Sample Testable</th>
                  <th>Test Result</th>
                  <th>Print</th>
                </tr>
                {
                  results.length === 0
                    ? " "
                    : results.length !== 0
                    ? results.map((result) => (
                        <tr>
                          <td>{result.sampleID}</td>
                          <td>{result.approvalDate}</td>
                          <td>{result.dateResultDispatched}</td>
                          <td>{result.pcrLabSampleNumber}</td>
                          <td>{sampleStatus(result.sampleStatus)}</td>
                          <td>{result.sampleTestable}</td>
                          <td>
                            {result.testResult !== ""
                              ? `${result.testResult} cp/mL`
                              : ""}
                          </td>
                          {result.testResult !== "" ? (
                            <td>
                              <Link
                                to={{
                                  pathname: "/Patient-result",
                                  state: { data: result, sample: manifestObj },
                                }}
                              >
                                <PrintIcon />
                              </Link>
                            </td>
                          ) : (
                            " "
                          )}
                        </tr>
                      ))
                    : manifestObj.results.map((result) => (
                        <tr>
                          <td>{result.sampleID}</td>
                          <td>{result.approvalDate}</td>
                          <td>{result.dateResultDispatched}</td>
                          <td>{result.pcrLabSampleNumber}</td>
                          <td>{sampleStatus(result.sampleStatus)}</td>
                          <td>{result.sampleTestable}</td>
                          <td>{result.testResult}</td>
                          <td>
                            <Link
                              to={{
                                pathname: "/Patient-result",
                                state: { data: result, sample: manifestObj },
                              }}
                            >
                              <PrintIcon />
                            </Link>
                          </td>
                        </tr>
                      ))
                  //                    <>
                  //                      <br />
                  //                      <p style={{ textAlign: "center" }}>
                  //                        No sample results available.
                  //                      </p>
                  //                    </>
                }
              </tbody>
            </Table>
            {/* {results.length === 0 ? (
              <p>
                {" "}
                <Spinner color="primary" /> Please Wait, Syncing with LIMS
                server...{" "}
              </p>
            ) : (
              " "
            )} */}
            <br />
            <span style={{ fontSize: "10px" }}>LAMISPlus 2.0: {today}</span>
          </Row>
          <hr />
        </CardBody>
      </Card>
    );
  }
}

export default PrintResults;
