package com.xatkit.example;

import com.google.gson.JsonElement;
import com.xatkit.core.XatkitBot;
import com.xatkit.plugins.react.platform.ReactPlatform;
import com.xatkit.plugins.react.platform.io.ReactEventProvider;
import com.xatkit.plugins.react.platform.io.ReactIntentProvider;
import com.xatkit.plugins.rest.platform.RestPlatform;
import com.xatkit.plugins.rest.platform.utils.ApiResponse;
import lombok.val;
import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.Configuration;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.xatkit.dsl.DSL.city;
import static com.xatkit.dsl.DSL.eventIs;
import static com.xatkit.dsl.DSL.fallbackState;
import static com.xatkit.dsl.DSL.intent;
import static com.xatkit.dsl.DSL.intentIs;
import static com.xatkit.dsl.DSL.model;
import static com.xatkit.dsl.DSL.state;

public class WeatherBot {

    public static void main(String[] args) {

        val howIsTheWeather = intent("HowIsTheWeather")
                .trainingSentence("How is the weather today in CITY?")
                .trainingSentence("What is the forecast for today in CITY?")
                .parameter("cityName").fromFragment("CITY").entity(city());


        ReactPlatform reactPlatform = new ReactPlatform();
        RestPlatform restPlatform = new RestPlatform();
        ReactEventProvider reactEventProvider = reactPlatform.getReactEventProvider();
        ReactIntentProvider reactIntentProvider = reactPlatform.getReactIntentProvider();

        val init = state("Init");
        val awaitingInput = state("AwaitingInput");
        val printWeather = state("PrintWeather");

        init
                .next()
                .when(eventIs(ReactEventProvider.ClientReady)).moveTo(awaitingInput);


        awaitingInput
                .next()
                .when(intentIs(howIsTheWeather)).moveTo(printWeather);

        printWeather
                .body(context -> {
                    String cityName = (String) context.getIntent().getValue("cityName");
                    Map<String, Object> queryParameters = new HashMap<>();
                    queryParameters.put("q", cityName);
                    ApiResponse<JsonElement> response = restPlatform.getJsonRequest(context, "http://api" +
                                    ".openweathermap.org/data/2.5/weather", queryParameters, Collections.emptyMap(),
                            Collections.emptyMap());
                    if (response.getStatus() == 200) {
                        long temp = Math.round(response.getBody().getAsJsonObject().get("main").getAsJsonObject().get(
                                "temp").getAsDouble());
                        long tempMin =
                                Math.round(response.getBody().getAsJsonObject().get("main").getAsJsonObject().get(
                                        "temp_min").getAsDouble());
                        long tempMax =
                                Math.round(response.getBody().getAsJsonObject().get("main").getAsJsonObject().get(
                                        "temp_max").getAsDouble());
                        String weather =
                                response.getBody().getAsJsonObject().get("weather").getAsJsonArray().get(0).getAsJsonObject().get("description").getAsString();
                        String weatherIcon =
                                "http://openweathermap.org/img/wn/" + response.getBody().getAsJsonObject().get(
                                        "weather").getAsJsonArray().get(0).getAsJsonObject().get("icon").getAsString() + ".png";
                        reactPlatform.reply(context, MessageFormat.format("The current weather is {0} &deg;C with " +
                                        "{1} ![{1}]({2}) with a high of {3} &deg;C and a low of {4} &deg;C", temp,
                                weather,
                                weatherIcon, tempMax, tempMin));
                    } else if (response.getStatus() == 400) {
                        reactPlatform.reply(context, "Oops, I couldn't find this city");
                    } else {
                        reactPlatform.reply(context, "Sorry, an error " +  response.getStatus() + " " + response.getStatusText() + " occurred when accessing the openweathermap service");
                    }

                })
                .next()
                .moveTo(awaitingInput);


        val defaultFallback = fallbackState()
                .body(context -> reactPlatform.reply(context, "Sorry, I didn't, get it"));

        val botModel = model()
                .usePlatform(reactPlatform)
                .usePlatform(restPlatform)
                .listenTo(reactEventProvider)
                .listenTo(reactIntentProvider)
                .initState(init)
                .defaultFallbackState(defaultFallback);

        Configuration botConfiguration = new BaseConfiguration();
        botConfiguration.addProperty("xatkit.message.delay", 500);
        botConfiguration.addProperty("xatkit.rest.default.query.parameters", "units=Metric&appid=XXX");

        XatkitBot xatkitBot = new XatkitBot(botModel, botConfiguration);
        xatkitBot.run();
    }
}
