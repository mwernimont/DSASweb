package gov.usgs.cida.dsas.rest.service.layer;

import com.google.gson.Gson;
import gov.usgs.cida.dsas.dao.geoserver.GeoserverDAO;
import gov.usgs.cida.dsas.service.util.Property;
import gov.usgs.cida.dsas.service.util.PropertyUtil;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

/**
 *
 * @author isuftin
 */
@Path("/")
public class LayerResource {

	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(LayerResource.class);
	private static GeoserverDAO geoserverHandler = null;
	private static String geoserverEndpoint = null;
	private static String geoserverUsername = null;
	private static String geoserverPassword = null;

	public LayerResource() {
		geoserverEndpoint = PropertyUtil.getProperty(Property.GEOSERVER_ENDPOINT);
		geoserverUsername = PropertyUtil.getProperty(Property.GEOSERVER_USERNAME);
		geoserverPassword = PropertyUtil.getProperty(Property.GEOSERVER_PASSWORD);
		geoserverHandler = new GeoserverDAO(geoserverEndpoint, geoserverUsername, geoserverPassword);

	}

	@DELETE
	@Path("workspace/{workspace}/store/{store}/{layer}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteLayerInWorkspace(
			@PathParam("layer") String layer,
			@PathParam("store") String store,
			@PathParam("workspace") String workspace) {
		
		Response response;
		if (StringUtils.isBlank(workspace) || StringUtils.isBlank(store) || StringUtils.isBlank(layer)) {
			Map<String, String> map = new HashMap<>(1);
			map.put("error", "Workspace, store or layer inputs were blank");
			response = Response
					.status(Response.Status.BAD_REQUEST)
					.entity(new Gson().toJson(map))
					.build();
		} else if (workspace.toLowerCase().trim().equals("published")) {
			response = Response.status(Response.Status.FORBIDDEN).build();
		} else {
			try {
				geoserverHandler.removeLayer(workspace, store, layer);
				response = Response.notModified().build();
			} catch (IllegalArgumentException | MalformedURLException ex) {
				Map<String, String> map = new HashMap<>(1);
				map.put("error", ex.getMessage());
				response = Response
						.serverError()
						.entity(new Gson().toJson(map))
						.build();
			}
		}
		
		return response;
	}
}
