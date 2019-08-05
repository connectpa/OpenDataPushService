package it.connectpa.odataservice.servlet;

import it.connectpa.odataservice.service.ODataEdmProvider;
import java.io.IOException;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataHttpHandler;
import org.apache.olingo.server.api.ServiceMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@WebServlet(urlPatterns = "/odata/*", loadOnStartup = 1)
public class ODataService extends HttpServlet {

    private static final Logger LOG = LoggerFactory.getLogger(ODataService.class);

    private static final long serialVersionUID = 6279606142522875093L;

    @Autowired
    private ODataEdmProvider oDataEdmProvider;

    @Override
    protected void service(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException,
            IOException {

        try {
            OData oData = OData.newInstance();
            ServiceMetadata edm = oData.createServiceMetadata(oDataEdmProvider, new ArrayList<>());
            ODataHttpHandler handler = oData.createHandler(edm);

            handler.process(req, resp);
        } catch (RuntimeException e) {
            LOG.error("Server Error occurred in Servlet", e);
            throw new ServletException(e);
        }
    }
}
