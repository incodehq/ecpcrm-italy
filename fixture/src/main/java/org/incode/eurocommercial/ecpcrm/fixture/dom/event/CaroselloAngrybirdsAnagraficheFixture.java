package org.incode.eurocommercial.ecpcrm.fixture.dom.event;

import lombok.experimental.Accessors;
import org.incode.eurocommercial.ecpcrm.dom.event.EventSourceType;

@Accessors(fluent = true)
public class CaroselloAngrybirdsAnagraficheFixture extends CsvEventFixture {
    public CaroselloAngrybirdsAnagraficheFixture() {
        super();
        fileName = "carosello_angrybirds_anagrafiche.csv";
        eventSourceType = EventSourceType.Carosello_Angry_Birds_Csv;
    }
}
