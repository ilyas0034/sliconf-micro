package javaday.istanbul.sliconf.micro.steps.other;


import cucumber.api.java.tr.Diyelimki;
import javaday.istanbul.sliconf.micro.SpringBootTestConfig;
import javaday.istanbul.sliconf.micro.event.EventBuilder;
import javaday.istanbul.sliconf.micro.event.EventSpecs;
import javaday.istanbul.sliconf.micro.event.model.Event;
import javaday.istanbul.sliconf.micro.event.model.LifeCycleState;
import javaday.istanbul.sliconf.micro.event.service.EventRepositoryService;
import javaday.istanbul.sliconf.micro.floor.CreateFloorRoute;
import javaday.istanbul.sliconf.micro.floor.Floor;
import javaday.istanbul.sliconf.micro.response.ResponseMessage;
import javaday.istanbul.sliconf.micro.user.model.User;
import javaday.istanbul.sliconf.micro.user.service.UserRepositoryService;
import org.junit.Ignore;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Ignore
public class CreateFloorTest extends SpringBootTestConfig { // NOSONAR

    @Autowired
    UserRepositoryService userRepositoryService;

    @Autowired
    EventRepositoryService eventRepositoryService;

    @Autowired
    CreateFloorRoute createFloorRoute;


    @Diyelimki("^Floor kaydediliyor$")
    public void floorKaydediliyor() throws Throwable {
        // Given
        User user = new User();
        user.setUsername("createFloorUser1");
        user.setEmail("createFloorUser1@sliconf.com");
        user.setPassword("123123123");

        ResponseMessage savedUserMessage = userRepositoryService.saveUser(user);

        assertTrue(savedUserMessage.isStatus());

        String userId = ((User) savedUserMessage.getReturnObject()).getId();

        Event event = new EventBuilder().setName("Create Floor Test")
                .setExecutiveUser(userId)
                .setDate(LocalDateTime.now().plusMonths(2)).build();

        EventSpecs.generateKanbanNumber(event, eventRepositoryService);
        event.setStatus(true);
        LifeCycleState lifeCycleState = new LifeCycleState();
        lifeCycleState.setEventStatuses(new HashSet<>());
        event.setLifeCycleState(lifeCycleState);


        ResponseMessage eventSaveMessage = eventRepositoryService.save(event);
        String eventKey = ((Event) eventSaveMessage.getReturnObject()).getKey();


        List<Floor> floors = createFLoors2();


        // When

        // True
        ResponseMessage saveFloorResponseMessage1 = createFloorRoute.saveFloors(floors, eventKey);

        // False
        ResponseMessage saveFloorResponseMessage2 = createFloorRoute.saveFloors(floors, null);
        ResponseMessage saveFloorResponseMessage3 = createFloorRoute.saveFloors(floors, "9999");
        ResponseMessage saveFloorResponseMessage4 = createFloorRoute.saveFloors(floors, "");
        ResponseMessage saveFloorResponseMessage5 = createFloorRoute.saveFloors(null, "");


        // Then
        assertTrue(saveFloorResponseMessage1.isStatus());

        assertFalse(saveFloorResponseMessage2.isStatus());
        assertFalse(saveFloorResponseMessage3.isStatus());
        assertFalse(saveFloorResponseMessage4.isStatus());
        assertFalse(saveFloorResponseMessage5.isStatus());

    }

    private List<Floor> createFLoors2() {
        List<Floor> floors = new ArrayList<>();

        Floor floor1 = new Floor();
        floor1.setId("floor11");
        floor1.setName("Floor 11");
        floor1.setImage("floorImage11");

        Floor floor2 = new Floor();
        floor2.setId("floor2");
        floor2.setName("Floor 2");
        floor2.setImage("floorImage2");

        Floor floor3 = new Floor();
        floor3.setId("floor3");
        floor3.setName("Floor 3");
        floor3.setImage("floorImage3");

        floors.add(floor1);
        floors.add(floor2);
        floors.add(floor3);

        return floors;
    }

}
