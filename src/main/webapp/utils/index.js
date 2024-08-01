const checkStatus = (status) => {
    let statusValue = "";

    if (status === 0) {
        statusValue = 'Pending';
    }
    else if (status === 1){
        statusValue = 'Collected';
    }
    else if (status === 2){
        statusValue = 'Verified';
    }
    else if (status === 3){
        statusValue = 'Ready';
    }
    return statusValue;
}

export { checkStatus }