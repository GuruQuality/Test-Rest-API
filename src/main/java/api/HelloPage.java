package api;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

@WebServlet
public class HelloPage extends HttpServlet {
    public void service(HttpServletRequest req, HttpServletResponse resp) {
        //берем данные из объекта запроса HttpServletRequest
        //и заполняем объект ответа HttpServletResponse
        try (PrintWriter pw = resp.getWriter()) {
            pw.append("<H1>hello<H1>");
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}

@WebServlet
class OredersPage extends HttpServlet {
    public void service(HttpServletRequest req, HttpServletResponse resp) {
        //логика
    }
}

@WebServlet
 class ItemsPage extends HttpServlet {
    public void service(HttpServletRequest req, HttpServletResponse resp) {
        //логика
    }
}


