function generate(records, token) {

    var json = JSON.parse(records);

    var entities = [];

    for (record in json) {
        entities.push({
            entityId : record.station_id,
            name : record.name,
            token : token,
            type : '',
            props : {
                landmark : record.landmark
            }
        });
    }

    return JSON.stringify(entities);
}