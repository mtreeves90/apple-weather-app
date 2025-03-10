package com.weatherapp;

import com.weatherapp.util.WeatherUtilService;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 * Apple Weather Application
 * @author Michael Thomas Reeves
 */
public class WeatherApp extends Application {
    private static final String LOCATION_LABEL_TEXT = "Provide a location to view its weather forecast";
    private Label locationLabel;
    private TextField streetInput;
    private TextField cityInput;
    private TextField stateInput;
    private TextField zipInput;
    private Button getForecastButton;
    private Button clearInputButton;
    private Label weatherInfoLabel;

    @Override
    public void start(Stage stage) {
    	stage.setTitle("Weather Forecast App");

        locationLabel = new Label(LOCATION_LABEL_TEXT);
        locationLabel.setFont(new Font(18));

        streetInput = new TextField();
        streetInput.setPromptText("Street Address (Optional)");

        cityInput = new TextField();
        cityInput.setPromptText("City");

        stateInput = new TextField();
        stateInput.setPromptText("State/Province");

        zipInput = new TextField();
        zipInput.setPromptText("ZIP Code");

        getForecastButton = new Button("Get Forecast");
        getForecastButton.setOnAction(e -> fetchWeather());

        clearInputButton = new Button("Clear");
        clearInputButton.setOnAction(e -> clear());

        weatherInfoLabel = new Label("");
        weatherInfoLabel.setFont(new Font(24));

        VBox layout = new VBox(20);
        layout.setPadding(new Insets(30));
        layout.getChildren().addAll(locationLabel, streetInput, cityInput,
        stateInput, zipInput, getForecastButton, clearInputButton, weatherInfoLabel);

        Scene scene = new Scene(layout, 800, 600);
        stage.setScene(scene);
        stage.show();
    }

    private void fetchWeather() {
        String location = zipInput.getText().trim();
        if (location.isEmpty()) {
            location = cityInput.getText().trim();
            if (location.isEmpty()) {
                location = stateInput.getText().trim();
                if (location.isEmpty()) {
                    weatherInfoLabel.setText("Please enter a valid location.");
                    return;
                }
            }
        }

        String weatherInfo = WeatherUtilService.retrieveWeatherByLocation(location);

        weatherInfoLabel.setText(weatherInfo);
    }

    private void clear() {
        streetInput.clear();;
        cityInput.clear();
        stateInput.clear();
        zipInput.clear();
        weatherInfoLabel.setText("");
    }

    public static void main(String[] args) {
        launch(args);
    }
}