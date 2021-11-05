package org.bitbucket.socialroboticshub.actions.animation;

import java.io.File;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FilenameUtils;
import org.bitbucket.socialroboticshub.actions.RobotAction;

import eis.iilang.Identifier;
import eis.iilang.Parameter;

public class PlayMotionAction extends RobotAction {
	public final static String NAME = "playMotion";
	private boolean isXML = false;
	private boolean isFile = false;

	/**
	 * @param parameters A list with at least one identifier, referencing either a
	 *                   XML or JSON file or a pure motion to be played. Optionally
	 *                   (only for an XML file) a second identifier with an emotion
	 *                   to use as a transformation to that animation.
	 */
	public PlayMotionAction(final List<Parameter> parameters) {
		super(parameters);
	}

	@Override
	public boolean isValid() {
		final int params = getParameters().size();
		boolean valid = (params == 1 || params == 2);
		if (valid) {
			valid &= (getParameters().get(0) instanceof Identifier);
			final File motionFile = new File(EIStoString(getParameters().get(0)));
			if (motionFile.canRead()) {
				this.isFile = true;
				final String motionFileExtension = FilenameUtils.getExtension(motionFile.getName());
				this.isXML = motionFileExtension.equalsIgnoreCase("xml");
				valid &= (this.isXML || motionFileExtension.equalsIgnoreCase("json"));
			}
			if (params == 2) {
				valid &= (getParameters().get(1) instanceof Identifier);
			}
		}
		return valid;
	}

	@Override
	public String getTopic() {
		return this.isXML ? "action_motion_file" : "action_play_motion";
	}

	@Override
	public String getData() {
		if (this.isFile) {
			try {
				final Path path = Paths.get(EIStoString(getParameters().get(0)));
				final String motion = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
				if (this.isXML) {
					final StringBuilder result = new StringBuilder(getMinifiedXML(motion));
					if (getParameters().size() == 2) {
						result.append(";").append(EIStoString(getParameters().get(1)));
					}
					return result.toString();
				}
				return motion;

			} catch (final Exception e) {
				throw new RuntimeException("Failed to read motion file", e);
			}
		} else {
			return EIStoString(getParameters().get(0));
		}
	}

	@Override
	public String getExpectedEvent() {
		return "PlayMotionStarted";
	}

	private static String getMinifiedXML(final String source) throws Exception {
		final TransformerFactory factory = TransformerFactory.newInstance();
		final InputStream xslt = PlayMotionAction.class.getResourceAsStream("/transform.xslt");
		final Transformer transformer = factory.newTransformer(new StreamSource(xslt));

		final Source text = new StreamSource(new StringReader(source));
		final Writer output = new StringWriter();
		transformer.transform(text, new StreamResult(output));

		return output.toString();
	}
}
