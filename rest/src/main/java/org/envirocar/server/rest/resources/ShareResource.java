/*
 * Copyright (C) 2013 The enviroCar project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.envirocar.server.rest.resources;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.envirocar.server.core.entities.Measurement;
import org.envirocar.server.core.entities.MeasurementValue;
import org.envirocar.server.core.entities.Measurements;
import org.envirocar.server.core.entities.Track;
import org.envirocar.server.core.exception.TrackNotFoundException;
import org.envirocar.server.core.filter.MeasurementFilter;
import org.envirocar.server.core.filter.StatisticsFilter;
import org.envirocar.server.core.statistics.Statistic;
import org.envirocar.server.core.statistics.Statistics;
import org.envirocar.server.rest.MediaTypes;
import org.envirocar.server.rest.util.OSMTileRenderer;
import org.envirocar.server.rest.util.ShareImageRenderUtil;
import org.joda.time.DateTime;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.vividsolutions.jts.geom.Coordinate;

public class ShareResource extends AbstractResource {
	public static final String TRACK = "{track}";
	private final Track track;
 
    @Inject
    public ShareResource(@Assisted Track track) {
        this.track = track;
    }
	
	@GET
	@Produces(MediaTypes.PNG_IMAGE)
	public Response getShareImage() {
		ByteArrayOutputStream byteArrayOS = null;
		try {
			Statistics statistics = null;
			ShareImageRenderUtil imp = new ShareImageRenderUtil();
			OSMTileRenderer osm=new OSMTileRenderer();
			statistics = getStatisticsService().getStatistics(new StatisticsFilter(track));
			BufferedImage mapImage = null;
			if(osm.imageExists(track.getIdentifier())){
				mapImage = osm.loadImage(track.getIdentifier());
			}else{
				Measurements measurements = getDataService().getMeasurements(
						new MeasurementFilter(track));
				mapImage = osm.createImage(measurements);
				osm.saveImage(mapImage, track.getIdentifier());
			}
			HashMap<String, String> hm = osm.getDetails(track,statistics);
			BufferedImage renderedImage = imp.process(mapImage, hm.get(osm.MAXSPEED),
					hm.get(osm.TIME), hm.get(osm.CONSUMPTION));
			byteArrayOS = new ByteArrayOutputStream();
			ImageIO.write(renderedImage, "png", byteArrayOS);

		} catch (IOException e) {
			e.printStackTrace();
		} catch (TrackNotFoundException e) {
			e.printStackTrace();
		}

		byte[] imageData = byteArrayOS.toByteArray();
		return Response.ok(imageData).build();
	}
	
	/*@GET
	@Produces("text/html")
	public String testService()
			throws TrackNotFoundException {
		Measurements measurements;
		Statistics statistics;
		measurements = getDataService().getMeasurements(
				new MeasurementFilter(track));
		statistics = getStatisticsService().getStatistics(new StatisticsFilter(track));
		String html = "<h2>All stuff</h2><ul>";
		html += "<li>" + "Hi all" + "</li>";
		html += "<li>" + track.getBegin() + "</li>";
		html += "<li>" + track.getEnd() + "</li>";
		html += "<li>" + track.getDescription() + "</li>";
		html += "<li>" + track.getObdDevice() + "</li>";
		html += "<li>" + track.getTouVersion() + "</li>";
		html += "<li>" + track.getIdentifier() + "</li>";
		html += "<li>" + statistics
				+ "</li>";
		for (Statistic m : statistics) {
			html += "<li>" + m.getPhenomenon().getName() + ":"
					+  m.getMean() + ":" 
					+  m.getPhenomenon().getUnit() + ":" 
					+ "</li>";
			for (MeasurementValue mv : m.getValues()) {
				html += "<li>" + mv.getPhenomenon().getName() + ":"
						+ mv.getValue() + " " + mv.getPhenomenon().getUnit()
						+ "</li>";
			}
			html += "</br>";
		}
		

		html += "</ul>";
		return html;
	}*/
	
	
}
