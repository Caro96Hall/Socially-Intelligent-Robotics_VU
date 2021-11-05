package org.bitbucket.socialroboticshub.ga;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.cloud.dialogflow.v2.QueryResult;
import com.google.cloud.dialogflow.v2.WebhookRequest;
import com.google.cloud.dialogflow.v2.WebhookResponse;
import com.google.gson.Gson;
import com.google.protobuf.Struct;
import com.google.protobuf.util.JsonFormat;

final class JsonUtils {
	private final Gson json;
	private final JsonFormat.Parser jsonParser;
	private final JsonFormat.Printer jsonPrinter;

	JsonUtils() {
		this.json = new Gson();
		this.jsonParser = JsonFormat.parser();
		this.jsonPrinter = JsonFormat.printer();
	}

	public QueryResult getQueryResult(final String input) throws Exception {
		final WebhookRequest.Builder request = WebhookRequest.newBuilder();
		this.jsonParser.merge(input, request);
		return request.getQueryResult();
	}

	public String convertResponse(final String text) throws Exception {
		final WebhookResponse.Builder response = WebhookResponse.newBuilder();
		final Struct.Builder struct = Struct.newBuilder();
		this.jsonParser.merge(getPayload(text), struct);
		response.setPayload(struct);
		return this.jsonPrinter.print(response);
	}

	// https://developers.google.com/assistant/conversational/df-asdk/reference/webhook/rest/Shared.Types/AppResponse#richresponse
	private String getPayload(final String txt) {
		// parse the input (channelname_elements, where the elements are split by |)
		final int split = txt.indexOf(':');
		final String channel = (split > 0) ? txt.substring(0, split) : "";
		final boolean isCard = channel.endsWith("card");
		final boolean isMedia = channel.endsWith("media");
		final String[] elements = (split > 0) ? txt.substring(split + 1).split("\\|") : new String[] { txt, "[]" };

		// google > response > rich response
		final Map<String, Map<String, ?>> google = new LinkedHashMap<>(1);
		final Map<String, Map<String, ?>> response = new LinkedHashMap<>(1);
		final Map<String, List<Map<String, ?>>> richResponse = new LinkedHashMap<>(2);
		// the rich response can have multiple items
		final List<Map<String, ?>> items = new ArrayList<>((isCard || isMedia) ? 2 : 1);

		// the first item is always a simple response (text to show+say)
		final Map<String, Map<String, String>> simpleResponseItem = new LinkedHashMap<>(1);
		final Map<String, String> simpleResponse = new LinkedHashMap<>(1);
		simpleResponse.put("textToSpeech", elements[0]);
		simpleResponseItem.put("simpleResponse", simpleResponse);
		items.add(simpleResponseItem);

		if (isCard) { // an optional additional basic card item (includes an image and alt text)
			final Map<String, Map<String, ?>> basicCardItem = new LinkedHashMap<>(1);
			final Map<String, Object> basicCard = new LinkedHashMap<>(2);
			basicCard.put("formattedText", elements[0]);
			final Map<String, String> image = new LinkedHashMap<>(2);
			image.put("url", elements[1]);
			image.put("accessibilityText", elements[2]);
			basicCard.put("image", image);
			basicCardItem.put("basicCard", basicCard);
			items.add(basicCardItem);
		} else if (isMedia) { // an optional additional MP3 to play (including a name)
			final Map<String, Map<String, ?>> mediaResponseItem = new LinkedHashMap<>(1);
			final Map<String, Object> mediaResponse = new LinkedHashMap<>(2);
			mediaResponse.put("mediaType", "AUDIO");
			final List<Map<String, ?>> mediaObjects = new ArrayList<>(1);
			final Map<String, Object> mediaObject = new LinkedHashMap<>(3);
			mediaObject.put("name", elements[1]);
			mediaObject.put("description", elements[0]);
			mediaObject.put("contentUrl", elements[2]);
			mediaObjects.add(mediaObject);
			mediaResponse.put("mediaObjects", mediaObjects);
			mediaResponseItem.put("mediaResponse", mediaResponse);
			items.add(mediaResponseItem);
		}

		// we have all the items now
		richResponse.put("items", items);

		// add the optional answer suggestions (added to the main rich response)
		final List<?> suggestions = this.json.fromJson(elements[(isCard || isMedia) ? 3 : 1], ArrayList.class);
		final List<Map<String, ?>> suggestionsList = new ArrayList<>(suggestions.size());
		for (final Object suggestion : suggestions) {
			final Map<String, String> suggestionItem = new LinkedHashMap<>(1);
			suggestionItem.put("title", suggestion.toString());
			suggestionsList.add(suggestionItem);
		}
		richResponse.put("suggestions", suggestionsList);

		// complete the object and return it as a JSON string
		response.put("richResponse", richResponse);
		google.put("google", response);
		return this.json.toJson(google);
	}
}
