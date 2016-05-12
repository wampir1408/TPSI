package rest;

import model.Course;
import model.Grade;
import model.GradeIterator;
import model.Student;
import org.bson.types.ObjectId;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import other.DbSingleton;
import other.Model;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by impresyjna on 20.04.2016.
 */
@Path("/students/{index}/courses/{courseId}/grades")
public class GradeResource {
    DbSingleton dbSingleton = DbSingleton.getInstance();

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public List<Grade> getAllGrades(@PathParam("index") final long index, @PathParam("courseId") final ObjectId courseId) {
        Course returnedCourse = null;
        returnedCourse = dbSingleton.getDs().get(Course.class, courseId);
        List<Grade> grades = new ArrayList<>();
        for (Grade grade : returnedCourse.getGrades()) {
            if (grade.getStudent().getIndex() == index) {
                grades.add(grade);
            }
        }
        return grades;
    }

    @GET
    @Path("/{gradeId}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getOneGrade(@PathParam("index") final long index, @PathParam("courseId") final ObjectId courseId, @PathParam("gradeId") final int gradeId) {
        Course returnedCourse = null;
        returnedCourse = dbSingleton.getDs().get(Course.class, courseId);
        List<Grade> grades = new ArrayList<>();
        for (Grade grade : returnedCourse.getGrades()) {
            if (grade.getStudent().getIndex() == index) {
                grades.add(grade);
            }
        }
        Grade returnedGrade = null;
        for (Grade grade : grades) {
            if (grade.getId() == gradeId) {
                returnedGrade = grade;
                break;
            }
        }

        if (returnedGrade == null) {
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity("404 Not found").build());
        }

        return Response.ok(returnedGrade).build();
    }

    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response createGrade(@PathParam("index") final long index, @PathParam("courseId") final ObjectId courseId, @Valid Grade grade, @Context UriInfo uriInfo) {
        Course choosenCourse = null;
        choosenCourse = dbSingleton.getDs().get(Course.class, courseId);
        if (grade.validateNote()) {
            if (grade.getStudent() == null) {
                Student studentForGrade = null;
                studentForGrade = dbSingleton.getDs().createQuery(Student.class).filter("index =", index).get();
                grade.setStudent(studentForGrade);
            }
            GradeIterator gradeIterator = dbSingleton.getDs().createQuery(GradeIterator.class).get();
            grade.setId(gradeIterator.getValue());
            Query<GradeIterator> qPom = dbSingleton.getDs().createQuery(GradeIterator.class);
            UpdateOperations<GradeIterator> pomOps;
            pomOps = dbSingleton.getDs().createUpdateOperations(GradeIterator.class).set("value", gradeIterator.getValue() + 1);
            dbSingleton.getDs().update(qPom, pomOps);
            choosenCourse.getGrades().add(grade);
            Query<Course> q = dbSingleton.getDs().createQuery(Course.class).filter("_id =", courseId);
            UpdateOperations<Course> ops;
            ops = dbSingleton.getDs().createUpdateOperations(Course.class).set("grades", choosenCourse.getGrades());
            dbSingleton.getDs().update(q, ops);

            URI uri = uriInfo.getAbsolutePathBuilder().path(Integer.toString(grade.getId())).build();
            return Response.created(uri).entity(grade).build();
        }
        return Response.status(Response.Status.NOT_MODIFIED).entity(grade).build();
    }

    @PUT
    @Path("/{gradeId}")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response updateGrade(@PathParam("index") final long index, @PathParam("courseId") final ObjectId courseId, @Valid Grade grade, @PathParam("gradeId") final int gradeId) {
        if (grade.validateNote()) {
            Course choosenCourse = null;
            choosenCourse = dbSingleton.getDs().get(Course.class, courseId);
            Student choosenStudent = null;
            Query qPom = dbSingleton.getDs().createQuery(Student.class).filter("index =", index);
            choosenStudent = (Student) qPom.get();
            for (int i = 0; i < choosenCourse.getGrades().size(); i++) {
                Grade tempGrade = choosenCourse.getGrades().get(i);
                if (tempGrade.getId() == gradeId) {
                    if (grade.getStudent() == null) {
                        grade.setStudent(choosenStudent);
                    }
                    choosenCourse.getGrades().set(i, grade);
                }
            }
            Query<Course> q = dbSingleton.getDs().createQuery(Course.class).filter("_id =", courseId);
            UpdateOperations<Course> ops;
            ops = dbSingleton.getDs().createUpdateOperations(Course.class).set("grades", choosenCourse.getGrades());
            dbSingleton.getDs().update(q, ops);
            return Response.status(Response.Status.OK).entity(grade).build();
        }
        return Response.status(Response.Status.NOT_MODIFIED).entity(grade).build();
//        //TODO:
//        if (grade.validateNote()) {
//            Course returnedCourse = null;
//            returnedCourse = dbSingleton.getDs().get(Course.class, courseId);
//            List<Grade> grades = new ArrayList<>();
//            for (Grade gradePom : returnedCourse.getGrades()) {
//                if (gradePom.getStudent().getIndex() == index) {
//                    grades.add(grade);
//                }
//            }
//            Grade returnedGrade = null;
//            for (Grade gradePom : grades) {
//                if (gradePom.getId() == gradeId) {
//                    returnedGrade = grade;
//                    break;
//                }
//            }
//            if (returnedGrade == null) {
//                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity("404 Not found").build());
//            } else {
//                Student returnedStudent = null;
//                Query qPom = dbSingleton.getDs().createQuery(Student.class).filter("index =", index);
//                returnedStudent = (Student) qPom.get();
//                for (int i = 0; i < grades.size(); i++) {
//                    Grade tempGrade = returnedCourse.getGrades().get(i);
//                    if (tempGrade.getId() == returnedGrade.getId()) {
//                        if (grade.getStudent() == null) {
//                            grade.setStudent(returnedStudent);
//                            grade.setId(tempGrade.getId());
//                        }
//                        returnedCourse.getGrades().set(i, grade);
//                    }
//                }
//                Query<Course> q = dbSingleton.getDs().createQuery(Course.class).filter("_id =", courseId);
//                UpdateOperations<Course> ops;
//                ops = dbSingleton.getDs().createUpdateOperations(Course.class).set("grades", returnedCourse.getGrades());
//                dbSingleton.getDs().update(q, ops);
//                return Response.status(Response.Status.OK).entity(grade).build();
//            }
//        }
//        return Response.status(Response.Status.NOT_MODIFIED).entity(grade).build();
    }

    @Path("/{gradeId}")
    @DELETE
    @Produces(MediaType.TEXT_PLAIN)
    public Response deleteGrade(@PathParam("index") final long index, @PathParam("courseId") final ObjectId courseId, @PathParam("gradeId") final int gradeId) {
        Response response = null;
        Course choosenCourse = null;
        choosenCourse = dbSingleton.getDs().get(Course.class, courseId);
        if (choosenCourse != null) {
            List<Grade> grades = choosenCourse.getGrades();
            for (Grade grade1 : grades) {
                if (grade1.getId() == gradeId) {
                    grades.remove(grade1);
                    response = Response.ok("Grade  " + gradeId + " removed").build();
                    Query<Course> q = dbSingleton.getDs().createQuery(Course.class).filter("_id =", courseId);
                    UpdateOperations<Course> ops;
                    ops = dbSingleton.getDs().createUpdateOperations(Course.class).set("grades", choosenCourse.getGrades());
                    dbSingleton.getDs().update(q, ops);
                    break;
                }
            }
        }

        if (response == null) {
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity("404 Not found").build());
        }

        return response;
    }
}