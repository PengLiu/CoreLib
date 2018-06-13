function generate(record, token) {
    
    var json = JSON.parse(record);
    
    var entity = [{
        entityId : json.station_id,
        name : json.name,
        token: token,
        type : '',
        props : {
            landmark : json.landmark
        }
    }];
    
    return JSON.stringify(entity);
}