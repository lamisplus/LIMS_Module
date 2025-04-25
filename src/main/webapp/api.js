export const url =
    process.env.NODE_ENV === "development"
        ? "http://localhost:8789/api/v1/"
        : "/api/v1/";
export const token =
    process.env.NODE_ENV === "development"
        ? "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJndWVzdEBsYW1pc3BsdXMub3JnIiwiYXV0aCI6IlN1cGVyIEFkbWluIiwibmFtZSI6Ikd1ZXN0IEd1ZXN0IiwiZXhwIjoxNzQ1NjIwMDExfQ.w8JYTQZ1DUR3CkSw6LmE_zEUA-JoFxs7qcOkTs5Ij3gWZMgrw-DN1N2jCtYn3n4M-fUYCv4TLqgDq7Ftbu58Bg"
        : new URLSearchParams(window.location.search).get("jwt");