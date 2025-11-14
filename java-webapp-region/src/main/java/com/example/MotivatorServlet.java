package com.example;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Random;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/motivator")
public class MotivatorServlet extends HttpServlet {
    private static final String[] QUOTES = {
        "Believe in yourself!",
        "Stay positive, work hard, make it happen!",
        "Success is not final, failure is not fatal: It is the courage to continue that counts.",
        "Dream big and dare to fail.",
        "Your limitation—it’s only your imagination."
    };

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String region = request.getParameter("region");
        String currentServerTime = ZonedDateTime.now().toString();
        String regionTime;
        try {
            ZoneId zone = ZoneId.of(region);
            regionTime = ZonedDateTime.now(zone).toString();
        } catch (Exception e) {
            regionTime = "Invalid region";
        }
        String quote = QUOTES[new Random().nextInt(QUOTES.length)];

        response.setContentType("text/html");
        response.getWriter().println("<html><body>");
        response.getWriter().println("<h2>Current Server Time: " + currentServerTime + "</h2>");
        response.getWriter().println("<h2>Time in " + region + ": " + regionTime + "</h2>");
        response.getWriter().println("<h3>Motivational Quote: " + quote + "</h3>");
        response.getWriter().println("<a href='index.jsp'>Go Back</a>");
        response.getWriter().println("</body></html>");
    }
}
