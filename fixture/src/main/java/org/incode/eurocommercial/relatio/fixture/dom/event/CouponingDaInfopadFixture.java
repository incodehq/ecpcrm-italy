package org.incode.eurocommercial.relatio.fixture.dom.event;

import lombok.experimental.Accessors;
import org.incode.eurocommercial.relatio.dom.event.EventSourceType;

@Accessors(fluent = true)
public class CouponingDaInfopadFixture extends CsvEventFixture {
    public CouponingDaInfopadFixture() {
        super();
        fileName = "couponing_da_infopad.csv";
        eventSourceType = EventSourceType.Couponing_Da_Infopad_Csv;
    }
}
