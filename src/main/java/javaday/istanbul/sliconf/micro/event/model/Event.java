package javaday.istanbul.sliconf.micro.event.model;

import javaday.istanbul.sliconf.micro.agenda.model.AgendaElement;
import javaday.istanbul.sliconf.micro.floor.Floor;
import javaday.istanbul.sliconf.micro.room.Room;
import javaday.istanbul.sliconf.micro.speaker.Speaker;
import javaday.istanbul.sliconf.micro.sponsor.Sponsor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Document(collection = "events")
@CompoundIndexes(
        @CompoundIndex(def = "{'id':1}")
)
@Getter
@Setter
public class Event {
    @Id
    private String id;
    @Indexed
    private String key;
    private String name;
    private String logoPath;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private boolean dateLock;
    private String executiveUser;
    private List<AgendaElement> agenda;
    private List<Speaker> speakers;
    private boolean status;
    private String description;
    private About about;
    private List<Room> rooms;
    private List<Floor> floorPlan;
    private Map<String, String> sponsorTags;
    private Map<String, List<Sponsor>> sponsors;
    private Boolean deleted = false;
    private StatusDetails statusDetails;
    private TotalUsers totalUsers;
    private BaseEventState eventState;
    private LifeCycleState lifeCycleState = new LifeCycleState();

}
