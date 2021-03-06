package javaday.istanbul.sliconf.micro.event.controller;

import io.swagger.annotations.*;
import javaday.istanbul.sliconf.micro.agenda.model.AgendaElement;
import javaday.istanbul.sliconf.micro.comment.CommentRepositoryService;
import javaday.istanbul.sliconf.micro.comment.model.Comment;
import javaday.istanbul.sliconf.micro.event.EventControllerMessageProvider;
import javaday.istanbul.sliconf.micro.event.model.Event;
import javaday.istanbul.sliconf.micro.event.service.EventRepositoryService;
import javaday.istanbul.sliconf.micro.response.ResponseMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import spark.Request;
import spark.Response;
import spark.Route;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.*;
import java.util.stream.Collectors;

@Api
@Path("/service/events/get/statistics/:key")
@Produces("application/json")
@Component
public class GetStatisticsRoute implements Route {

    private EventControllerMessageProvider messageProvider;
    private EventRepositoryService repositoryService;
    private CommentRepositoryService commentRepositoryService;

    @Autowired
    public GetStatisticsRoute(EventControllerMessageProvider messageProvider,
                              EventRepositoryService eventRepositoryService,
                              CommentRepositoryService commentRepositoryService) {
        this.messageProvider = messageProvider;
        this.repositoryService = eventRepositoryService;
        this.commentRepositoryService = commentRepositoryService;
    }

    @GET
    @ApiOperation(value = "Returns event statistics with given key", nickname = "GetStatisticsRoute")
    @ApiImplicitParams({ //
            @ApiImplicitParam(required = true, dataType = "string", name = "token", paramType = "header"), //
            @ApiImplicitParam(required = true, dataType = "string", name = "key", paramType = "path"),
    })
    @ApiResponses(value = { //
            @ApiResponse(code = 200, message = "Success", response = ResponseMessage.class), //
            @ApiResponse(code = 400, message = "Invalid input data", response = ResponseMessage.class), //
            @ApiResponse(code = 401, message = "Unauthorized", response = ResponseMessage.class), //
            @ApiResponse(code = 404, message = "User not found", response = ResponseMessage.class) //
    })
    @Override
    public ResponseMessage handle(@ApiParam(hidden = true) Request request, @ApiParam(hidden = true) Response response) throws Exception {
        String key = request.params("key");

        return getStatistics(key);
    }

    public ResponseMessage getStatistics(String key) {
        // event var mı diye kontrol et
        Event event = repositoryService.findEventByKeyEquals(key);

        if (Objects.isNull(event)) {
            return new ResponseMessage(false, messageProvider.getMessage("eventCanNotFound"), new Object());
        }

        Map<String, Object> statisticsMap = new HashMap<>();

        final String APPROVED = "approved";

        long approvedComments = getCommentCount(event, APPROVED, "");
        long deniedComments = getCommentCount(event, "denied", "");

        Comment mostLikedComment = getMostLikedComment(event, APPROVED);

        mostLikedComment.setLikes(new ArrayList<>());
        mostLikedComment.setDislikes(new ArrayList<>());

        String mostCommentedSessionId = commentRepositoryService.findMostCommentedSessionId(APPROVED, event.getId());

        if (Objects.nonNull(event.getAgenda())) {
            List<AgendaElement> agendaElements = event.getAgenda().stream()
                    .filter(agendaElement -> Objects.nonNull(agendaElement) &&
                            Objects.nonNull(agendaElement.getId()) && agendaElement.getId().equals(mostCommentedSessionId))
                    .collect(Collectors.toList());

            if (Objects.nonNull(agendaElements) && !agendaElements.isEmpty() && Objects.nonNull(agendaElements.get(0))) {
                statisticsMap.put("mostCommentedSession", agendaElements.get(0));
            }
        }

        statisticsMap.put("approvedComments", approvedComments);
        statisticsMap.put("deniedComments", deniedComments);
        statisticsMap.put("mostLikedComment", mostLikedComment);
        statisticsMap.put("totalUsers", event.getTotalUsers());


        return new ResponseMessage(true, messageProvider.getMessage("eventStatisticsQueried"), statisticsMap);
    }

    private long getCommentCount(Event event, String status, String type) {
        if (Objects.nonNull(event)) {
            List<Comment> commentList = null;
            if (Objects.nonNull(event.getId()) && Objects.nonNull(status) && !status.isEmpty()) {
                commentList = commentRepositoryService.findAllByStatusAndEventId(status, event.getId(), 0, type, 0);
            }

            return Objects.nonNull(commentList) ? commentList.size() : 0;
        }
        return 0;
    }

    private Comment getMostLikedComment(Event event, String status) {
        if (Objects.nonNull(event)) {
            Comment comment = null;
            if (Objects.nonNull(event.getId()) && Objects.nonNull(status) && !status.isEmpty()) {
                comment = commentRepositoryService.findMostLikedComment(status, event.getId());
            }

            return Objects.nonNull(comment) ? comment : new Comment();
        }
        return new Comment();
    }
}
