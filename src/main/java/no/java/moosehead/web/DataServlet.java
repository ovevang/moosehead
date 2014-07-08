package no.java.moosehead.web;

import no.java.moosehead.api.ParticipantActionResult;
import no.java.moosehead.api.ParticipantApi;
import no.java.moosehead.api.ParticipantReservation;
import no.java.moosehead.api.WorkshopInfo;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DataServlet extends HttpServlet {
    private ParticipantApi participantApi;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/json");
        if ("/workshopList".equals(req.getPathInfo())) {
            printWorkshops(resp);
        } else {
            printMyReservations(req,resp);
        }

    }

    private void printMyReservations(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String email = req.getParameter("email");
        List<ParticipantReservation> participantReservations = participantApi.myReservations(email);
        List<JSONObject> reservations = participantReservations.stream().map(res -> {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("workshopid", res.getWorkshopid());
                jsonObject.put("email", res.getEmail());
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            return jsonObject;
        }).collect(Collectors.toList());
        try {
            new JSONArray(reservations).write(resp.getWriter());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void printWorkshops(HttpServletResponse resp) throws IOException {
        List<WorkshopInfo> workshops = participantApi.workshops();
        List<JSONObject> jsons = workshops.stream().map(workshop -> {
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("id", workshop.getId());
                        jsonObject.put("title", workshop.getTitle());
                        jsonObject.put("description", workshop.getDescription());
                        jsonObject.put("status", workshop.getStatus().name());

                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    return jsonObject;
                }
        ).collect(Collectors.toList());
        PrintWriter writer = resp.getWriter();
        try {
            new JSONArray(jsons).write(writer);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private Optional<ParticipantActionResult> doReservation(JSONObject jsonInput,HttpServletResponse resp) throws IOException {
        String workshopid = readField(jsonInput, "workshopid");
        String email = readField(jsonInput,"email");
        String fullname = readField(jsonInput,"fullname");

        if (workshopid == null || email == null || fullname == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,"Illegal json input");
            return Optional.empty();
        }
        ParticipantActionResult reservation = participantApi.reservation(workshopid, email, fullname);

        return Optional.of(reservation);
    }

    private Optional<ParticipantActionResult> doCancelation(JSONObject jsonInput,HttpServletResponse resp) throws IOException {
        String workshopid = readField(jsonInput, "workshopid");
        String email = readField(jsonInput,"email");

        if (workshopid == null || email == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,"Illegal json input");
            return Optional.empty();
        }
        ParticipantActionResult cancel = participantApi.cancellation(workshopid, email);

        return Optional.of(cancel);
    }

    private Optional<ParticipantActionResult> doConfirmEmail(JSONObject jsonInput,HttpServletResponse resp) throws IOException {
        String token = readField(jsonInput, "token");

        if (token == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,"Illegal json input");
            return Optional.empty();
        }
        ParticipantActionResult cancel = participantApi.confirmEmail(token);

        return Optional.of(cancel);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JSONObject jsonInput = readJson(req.getInputStream(),resp);
        if (jsonInput == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,"Illegal json input");
            return;
        }
        Optional<ParticipantActionResult> apiResult;
        if ("/cancel".equals(req.getPathInfo())) {
            apiResult = doCancelation(jsonInput, resp);
        } else if ("/reserve".equals(req.getPathInfo())) {
            apiResult = doReservation(jsonInput, resp);
        } else if ("/confirmEmail".equals(req.getPathInfo())) {
            apiResult = doConfirmEmail(jsonInput, resp);
        } else {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,"Illegal path");
            return;
        }
        if (!apiResult.isPresent()) {
            return;
        }
        resp.setContentType("text/json");
        JSONObject result = new JSONObject();
        try {
            result.put("status", apiResult.get().getStatus());
            String errormessage = apiResult.get().getErrormessage();
            if (errormessage != null) {
                result.put("message",errormessage);
            }
            result.write(resp.getWriter());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private String readField(JSONObject jsonInput, String name) {
        String value;
        try {
            value = jsonInput.getString(name);
        } catch (JSONException e) {
            return null;
        }
        for (char c : value.toCharArray()) {
            if (Character.isLetterOrDigit(c) || "-_ @.".indexOf(c) != -1) {
                continue;
            }
            return null;
        }
        return value;
    }

    private JSONObject readJson(ServletInputStream inputStream, HttpServletResponse resp) throws IOException {
        try {
            return new JSONObject(toString(inputStream));
        } catch (JSONException e) {
            return null;
        }
    }

    public void setParticipantApi(ParticipantApi participantApi) {
        this.participantApi = participantApi;
    }

    private static String toString(InputStream inputStream) throws IOException {
        try (Reader reader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"))) {
            StringBuilder result = new StringBuilder();
            int c;
            while ((c = reader.read()) != -1) {
                result.append((char)c);
            }
            return result.toString();
        }
    }
}
