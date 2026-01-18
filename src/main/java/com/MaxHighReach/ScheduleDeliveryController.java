package com.MaxHighReach;


import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.utils.PdfMerger;
import com.itextpdf.layout.Document;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.sql.*;
import java.sql.Connection;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;


public class ScheduleDeliveryController extends BaseController {

	@FXML
	private Rectangle dragArea;

	private static final double INITIAL_SCISSOR_LIFT_HEIGHT = 263; // Initial height of the scissor lift
	private static final double INITIAL_TABLE_HEIGHT = 50; // Initial height of the TableView (height of one row)
	private static final double ROW_HEIGHT = 25; // Height of each row in the TableView
	private double currentHeight = INITIAL_SCISSOR_LIFT_HEIGHT; // Track the current height of the scissor lift
	private int scheduledCounter = 0;


	private ObservableList<Customer> customers = FXCollections.observableArrayList();
	private Customer selectedCustomer;
	private AtomicBoolean dateSelected = new AtomicBoolean(false);
	private AtomicBoolean dateSelectedAT = new AtomicBoolean(false);
	private int rentalOrderId;
	private Rental currentCustomerRental;


	@FXML
	private Label tableViewTitle;
	@FXML
	private TableView<Rental> scheduledRentalsTableView;  // TableView for rentals
	@FXML
	private TableColumn<Rental, Integer> expandColumn;	
	@FXML
	private TableColumn<Rental, String> rentalDateColumn;   // Column for Rental Date
	@FXML
	private TableColumn<Rental, String> liftTypeColumn; 	// Column for Lift Type
	@FXML
	private TableColumn<Rental, String> deliveryTimeColumn;  // Column for Delivery Time


	private ObservableList<Rental> rentalsList = FXCollections.observableArrayList(); // List to hold rentals


	@FXML
	private TextField customerNameField;
	@FXML
	private Label orderedByLabel;
	@FXML
	private TextField orderedByField;
	@FXML
	private Label orderedByNumberLabel;
	@FXML
	private TextField orderedByPhoneField;
	@FXML
	private ComboBox orderedByBox;
	private Map<String, String> orderingContactsPhoneNumbers = new HashMap<>();
	private String selectedOrderingContactId;
	@FXML
	private Button autoTermButton;
	private final Tooltip autoTermTooltip = new Tooltip("Auto-term");
	@FXML
	private Label callOffDateLabel;
	private ToggleButton selectedCallOffDate;
	@FXML
	private TextField rentalDateField; // Hidden text field for rental date
	@FXML
	private DatePicker datePicker; // For the calendar view
	@FXML
	private DatePicker datePickerAT;
	private AtomicBoolean isCalendarExpanded = new AtomicBoolean(false);
	private AtomicBoolean isATCalendarExpanded = new AtomicBoolean(false);
	@FXML
	private TilePane weeksRowTilePane;
	@FXML
	private TilePane weeksRowTilePaneAT;
	@FXML
	private Rectangle calendarCover;
	@FXML
	private Rectangle calendarCoverAT;
	@FXML
	private TilePane liftTypeTilePane;  // TilePane containing the lift type toggle buttons
	@FXML
	private ToggleButton twelveMastButton;
	@FXML
	private ToggleButton nineteenSlimButton;
	@FXML
	private ToggleButton twentySixSlimButton;
	@FXML
	private ToggleButton twentySixButton;
	@FXML
	private ToggleButton thirtyTwoButton;
	@FXML
	private ToggleButton fortyButton;
	@FXML
	private ToggleButton thirtyThreeRTButton;
	@FXML
	private ToggleButton fortyFiveBoomButton;
	private List<ToggleButton> liftTypeToggleButtons = new ArrayList<>();
	@FXML
	private Label label12m, label19s, label26s, label26, label32, label40, label33rt, label45b;
	private Map<ToggleButton, Integer> liftTypeCounts = new HashMap<>();
	@FXML
	private Button plusButton;
	private int liftCount = 0;
	private List<String> addedLifts = new ArrayList<>();
	@FXML
	private Button xButton;
	@FXML
	private Label liftCountLabel;
	@FXML
	private TilePane deliveryTimeTilePane;  // TilePane for delivery time toggle buttons
	private ToggleGroup liftTypeToggleGroup;  // To ensure only one lift type can be selected at a time
	private ToggleGroup deliveryTimeToggleGroup;  // To ensure only one delivery time can be selected at a time
	private ToggleGroup weeksRowToggleGroup;
	private ToggleGroup weeksRowToggleGroupAT;
	@FXML
	private ToggleButton deliveryTime8To10Button; // Reference to the "8-10" toggle button
	@FXML
	public Button calendarButton;
	@FXML
	private ToggleButton customButton;
	@FXML
	private ComboBox<String> hourComboBox;
	@FXML
	private TextField siteField;
	@FXML
	private TextField addressField;
	@FXML
	private ComboBox<String> suggestionsBox;
	@FXML
	private TextField siteContactField;
	@FXML
	private ComboBox siteContactBox;
	@FXML
	private TextField siteContactPhoneField;
	private Map<String, String> siteContactsPhoneNumbers = new HashMap<>();
	private String selectedSiteContactId;
	@FXML
	private Label POLabel;
	@FXML
	private TextField POField;
	@FXML
	private Button notesButton;
	private final Tooltip notesTooltip = new Tooltip("Notes");
	@FXML
	private Label notesLabel;
	@FXML
	private TextField notesField;
	@FXML
	private Button scheduleDeliveryButton;
	@FXML
	private Label statusLabel; // Reference to the status label
	@FXML
	private Button printButton;
	private final Tooltip printTooltip = new Tooltip("Print Contracts");

	private Timeline rotationTimeline; // Timeline for rotating highlight
	private boolean isRotating = false;
	private boolean suggestionMuter = false;// Flag to track rotation state
	private int addressTypeCounter = 0;
	private ObservableList<String> addressSuggestions = FXCollections.observableArrayList();
	private OkHttpClient client = new OkHttpClient();
	private double LAT_MIN = 39.391122;
	private double LAT_MAX = 40.1234847;
	private double LON_MIN = -105.57661;
	private double LON_MAX = -104.4526;
	private List<GridCell> greaterGridCells = new ArrayList<>();
	private List<GridCell> lesserGridCells = new ArrayList<>();
	private Set<String> dominantLesserCells = Set.of(
        	"10", "11", "12", "40", "41", "42",
        	"69", "70", "71", "72", "97", "98", "99", "100", "101", "102", "103",
        	"127", "128", "129", "130", "131", "132", "133", "134", "135", "136", "137", "138", "139", "140",
        	"159", "160", "161", "162", "163", "164", "165", "166", "167", "168", "169", "170",
        	"189", "190", "191", "192", "193", "194", "195", "196", "197", "198",
        	"219", "220", "221", "222", "223", "224", "225", "226",
        	"248", "249", "250", "251", "252", "253", "254", "255", "256", "257", "258", "259", "260",
        	"275", "276", "277", "278", "279", "280", "281", "282", "283", "284", "285", "286", "287", "288", "289", "290", "291",
        	"304", "305", "306", "307", "308", "309", "310", "311", "312", "313", "314", "315", "316", "317", "318", "319", "320", "321",
        	"335", "336", "337", "338", "339", "340", "341", "342", "343", "344", "345", "346", "347", "348", "349", "350", "351",
        	"365", "366", "367", "368", "369", "370", "371", "372", "373", "374", "375", "376", "377", "378", "379", "380", "381",
        	"403", "404", "405", "406", "407", "408", "409", "410", "411",
        	"432", "436", "439", "440", "441",
        	"466", "467", "468", "469", "470",
        	"496", "497", "498", "499",
        	"527", "528"
	);

	// Initialize method to set up ToggleButtons and the ToggleGroup
	@FXML
	public void initialize() {

		super.initialize(dragArea);
    	customers = MaxReachPro.getCustomers(false);

    	customerNameField.setOnKeyPressed(event -> {
        	if (event.getCode() == KeyCode.ENTER) {
            	// Remove focus from the text field
            	customerNameField.getParent().requestFocus();
        	}
    	});


    	customerNameField.textProperty().addListener((observable, oldValue, newValue) -> {
        	// Call your existing autofill logic
        	autoFillCustomerName(newValue);


        	// Check if there's text in the field and update the style accordingly
        	if (newValue.isEmpty()) {
            	// No text, show the underline
            	customerNameField.getStyleClass().remove("has-text");
        	} else {
            	// Text is present, hide the underline
            	if (!customerNameField.getStyleClass().contains("has-text")) {
                	customerNameField.getStyleClass().add("has-text");
            	}
        	}
    	});


    	customerNameField.focusedProperty().addListener((obs, oldValue, newValue) -> {
        	if (!newValue) {
            	handleCustomerNameFieldUnfocus();


        	}
    	});


    	customerNameField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
        	if (isNowFocused) {
            	resetOrderedByElements();
        	}
    	});


    	orderedByBox.setOnAction(event -> handleContactSelection(orderedByBox, true));
    	siteContactBox.setOnAction(event -> handleContactSelection(siteContactBox, false));


    	orderedByBox.setPrefWidth(1);
    	orderedByBox.setMinWidth(1);
    	orderedByBox.setMaxWidth(1);


    	createCustomTooltip(autoTermButton, 38, 10, autoTermTooltip);


    	weeksRowToggleGroup = new ToggleGroup();  // Create the ToggleGroup for weeks
    	weeksRowToggleGroupAT = new ToggleGroup();
    	liftTypeToggleGroup = new ToggleGroup();  // Create the ToggleGroup for lift types
    	deliveryTimeToggleGroup = new ToggleGroup();  // Create the ToggleGroup for delivery times


    	// Load the calendar icon image
    	Image calendarImage = new Image(getClass().getResourceAsStream("/images/calendar.png"));
    	ImageView calendarIcon = new ImageView(calendarImage);
    	calendarIcon.setFitHeight(20);
    	calendarIcon.setFitWidth(20);
    	// Optionally, set the calendar button's graphic if you have a button for it.
    	// calendarButton.setGraphic(calendarIcon);


    	// Event listener for DatePicker
    	datePicker.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
        	if (isNowFocused) {
            	openCalendar(datePicker); // Call openCalendar when DatePicker is focused
        	} else if (!dateSelected.get()) {
            	closeCalendar(datePicker); // Call closeCalendar when focus is lost
        	}
    	});


    	datePicker.setDayCellFactory(picker -> new DateCell() {
        	@Override
        	public void updateItem(LocalDate item, boolean empty) {
            	super.updateItem(item, empty);
            	if (item != null && (item.getDayOfWeek() == DayOfWeek.SATURDAY || item.getDayOfWeek() == DayOfWeek.SUNDAY)) {
                	setDisable(true); // Disable weekends
                	setStyle("-fx-background-color: lightgrey;"); // Optional: change background color for disabled dates
            	}
        	}
    	});


    	datePickerAT.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
        	if (isNowFocused) {
            	openCalendar(datePickerAT); // Call openCalendar when DatePicker is focused
        	} else if (!dateSelectedAT.get()) {
            	closeCalendar(datePickerAT); // Call closeCalendar when focus is lost
        	}
    	});


    	datePickerAT.setDayCellFactory(picker -> new DateCell() {
        	@Override
        	public void updateItem(LocalDate item, boolean empty) {
            	super.updateItem(item, empty);
            	if (item != null && (item.getDayOfWeek() == DayOfWeek.SATURDAY || item.getDayOfWeek() == DayOfWeek.SUNDAY)) {
                	setDisable(true); // Disable weekends
                	setStyle("-fx-background-color: lightgrey;"); // Optional: change background color for disabled dates
            	}
        	}
    	});


    	// Initialize weeksRowTilePane toggle buttons
    	for (javafx.scene.Node node : weeksRowTilePane.getChildren()) {
        	if (node instanceof ToggleButton) {
            	ToggleButton toggleButton = (ToggleButton) node;
            	toggleButton.setToggleGroup(weeksRowToggleGroup);  // Add each ToggleButton to the ToggleGroup


            	// Add action for each button
            	toggleButton.setOnAction(event -> {
                	weeksRowToggleGroup.selectToggle(toggleButton); // Ensure the selected button is set
            	});
        	}
    	}


    	for (javafx.scene.Node node : weeksRowTilePaneAT.getChildren()) {
        	if (node instanceof ToggleButton) {
            	ToggleButton toggleButton = (ToggleButton) node;
            	toggleButton.setToggleGroup(weeksRowToggleGroupAT);
            	toggleButton.setOnAction(event -> {
                	if (toggleButton == selectedCallOffDate) {
                    	weeksRowToggleGroupAT.selectToggle(null);
                    	toggleButton.getStyleClass().remove("lift-type-button-stopped");
                    	toggleButton.getStyleClass().add("double-clicked-auto-term-button");
                    	selectedCallOffDate = null;
                    	event.consume();
                	} else {
                    	weeksRowToggleGroupAT.selectToggle(toggleButton);
                    	toggleButton.getStyleClass().remove("double-clicked-auto-term-button");
                    	if (!toggleButton.getStyleClass().contains("lift-type-button-stopped")) {
                        	toggleButton.getStyleClass().add("lift-type-button-stopped");
                    	}
                    	weeksRowToggleGroupAT.selectToggle(toggleButton);
                    	selectedCallOffDate = toggleButton;
                	}
            	});
        	}
    	}


    	setDefaultRentalDate("");
    	setDefaultRentalDate("AT");


    	for (javafx.scene.Node node : liftTypeTilePane.getChildren()) {
        	if (node instanceof ToggleButton) {
            	ToggleButton toggleButton = (ToggleButton) node;
            	toggleButton.setToggleGroup(liftTypeToggleGroup);  // Add each ToggleButton to the ToggleGroup
            	liftTypeToggleButtons.add(toggleButton);


        	}
    	}
    	prepareLiftTypeButtons();


    	liftTypeCounts.put(twelveMastButton, 0);
    	liftTypeCounts.put(nineteenSlimButton, 0);
    	liftTypeCounts.put(twentySixSlimButton, 0);
    	liftTypeCounts.put(twentySixButton, 0);
    	liftTypeCounts.put(thirtyTwoButton, 0);
    	liftTypeCounts.put(fortyButton, 0);
    	liftTypeCounts.put(thirtyThreeRTButton, 0);
    	liftTypeCounts.put(fortyFiveBoomButton, 0);


    	// Set up ToggleButtons for delivery time
    	for (javafx.scene.Node node : deliveryTimeTilePane.getChildren()) {
        	if (node instanceof ToggleButton) {
            	ToggleButton toggleButton = (ToggleButton) node;
            	toggleButton.setToggleGroup(deliveryTimeToggleGroup);  // Add each ToggleButton to the ToggleGroup


            	// Hide the custom ComboBoxes when a delivery time button is clicked
            	toggleButton.setOnAction(event -> {
                	if (toggleButton != customButton) {
                    	hourComboBox.setVisible(false);
                    	//                  	ampmComboBox.setVisible(false);
                    	customButton.setSelected(false); // Unselect custom button
                	}
            	});
            	toggleButton.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                	if (isNowFocused) {
                    	resetOrderedByElements();
                	}
            	});
        	}
    	}


    	// Pre-select the "8-10" delivery time toggle button
    	deliveryTimeToggleGroup.selectToggle(deliveryTime8To10Button);


    	// Populate hour and AM/PM ComboBoxes
    	//  for (int i = 1; i <= 12; i++) {
    	hourComboBox.getItems().addAll("6", "7", "8", "9", "10", "11", "12", "1", "2", "3", "4"); // Add hours 1-12 to the ComboBox
    	//   	}
    	//   	ampmComboBox.getItems().addAll("am", "pm"); // Add AM and PM to the ComboBox


    	customButton.setOnAction(event -> {
        	boolean isSelected = customButton.isSelected();
        	hourComboBox.setVisible(isSelected);
        	//      	ampmComboBox.setVisible(isSelected);
        	if (!isSelected) {
            	hourComboBox.getSelectionModel().clearSelection();
            	//          	ampmComboBox.getSelectionModel().clearSelection();
        	}
    	});


    	siteField.textProperty().addListener((observable, oldValue, newValue) -> {
        	// Check if there's text in the field and update the style accordingly
        	if (newValue.isEmpty()) {
            	siteField.getStyleClass().remove("has-text");
            	siteField.getStyleClass().add("empty-unfocused");
        	} else {
            	siteField.getStyleClass().remove("empty-unfocused");
            	if (!siteField.getStyleClass().contains("has-text")) {
                	siteField.getStyleClass().add("has-text");
            	}
        	}
    	});


    	// Handle focus events for addressField
    	siteField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
        	if (isNowFocused) {
            	siteField.getStyleClass().remove("empty-unfocused"); // Remove empty style when focused
            	resetOrderedByElements();
        	} else if (siteField.getText().isEmpty()) {
            	siteField.getStyleClass().add("empty-unfocused"); // Add empty style when focus is lost
        	}
    	});


    	suggestionsBox.setPrefWidth(1);
    	suggestionsBox.setMinWidth(1);
    	suggestionsBox.setMaxWidth(1);
    	suggestionsBox.setVisibleRowCount(5); // Set the number of visible rows


    	// Listener for addressField text changes
    	addressField.textProperty().addListener((observable, oldValue, newValue) -> {
        	// Check if the last action was a backspace
        	boolean isBackspace = false;


        	// If the text length has decreased, we assume a backspace occurred
        	if (newValue.length() < oldValue.length()) {
            	isBackspace = true;
        	}


        	// Update suggestions based on input, unless it was a backspace
        	if (!isBackspace) {
            	updateSuggestions(newValue);
            	addressTypeCounter++; // Only increment if it wasn't a backspace
        	}


        	// Check if there's text in the field and update the style accordingly
        	if (newValue.isEmpty()) {
            	addressField.getStyleClass().remove("has-text");
            	addressField.getStyleClass().add("empty-unfocused");
            	//   suggestionsBox.setVisible(false); // Hide suggestions if empty
        	} else {
            	addressField.getStyleClass().remove("empty-unfocused");
            	if (!addressField.getStyleClass().contains("has-text")) {
                	addressField.getStyleClass().add("has-text");
            	}
        	}
    	});


    	// Handle focus events for addressField
    	addressField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
        	//   suggestionsBox.setVisible(false);
        	if (isNowFocused) {
            	addressField.getStyleClass().remove("empty-unfocused"); // Remove empty style when focused
            	resetOrderedByElements();
            	//System.out.println("");
        	} else if (addressField.getText().isEmpty()) {
            	addressField.getStyleClass().add("empty-unfocused"); // Add empty style when focus is lost
            	//  	suggestionsBox.setVisible(false); // Hide suggestions when focus is lost and input is empty
        	}
    	});


    	// Listener for the ComboBox to handle selection
    	suggestionsBox.setOnAction(e -> {
        	System.out.println("Action event on suggestionsBox");
        	String selectedSuggestion = suggestionsBox.getValue();
        	if (selectedSuggestion != null) {
            	System.out.println("Selected suggestion: " + selectedSuggestion);
            	String formattedAddress = formatSelectedSuggestion(selectedSuggestion);
            	addressField.setText(formattedAddress); // Set selected suggestion in the TextField
            	suggestionsBox.getSelectionModel().clearSelection(); // Clear selection to reset ComboBox
            	//   suggestionsBox.setVisible(false);
            	addressTypeCounter = 0;// Hide suggestions after selection
        	}


    	});


    	// Optional: Set up an additional listener if you want to catch selection via valueProperty
    	suggestionsBox.valueProperty().addListener((obs, oldVal, newVal) -> {
        	if (newVal != null) {
            	addressField.setText(newVal); // Populate addressField with selected suggestion
            	//   suggestionsBox.setVisible(false);
            	addressTypeCounter = 0;// Hide suggestions after selection
        	}
    	});


    	// Handle Enter key press in addressField to confirm selection
    	addressField.setOnKeyPressed(event -> {
        	if (event.getCode() == KeyCode.ENTER) {
            	if (suggestionsBox.isVisible() && suggestionsBox.getValue() != null) {
                	addressField.setText(suggestionsBox.getValue()); // Set selected suggestion
                	// 	suggestionsBox.setVisible(false);
                	addressTypeCounter = 0;// Hide suggestions on Enter key
            	} else {
                	addressField.getParent().requestFocus(); // Move focus if no selection
            	}
        	}
    	});


    	siteField.setOnKeyPressed(event -> {
        	if (event.getCode() == KeyCode.ENTER) {


            	siteField.getParent().requestFocus(); // Move focus if no selection
        	}
    	});


    	siteContactBox.setPrefWidth(1);
    	siteContactBox.setMinWidth(1);
    	siteContactBox.setMaxWidth(1);


    	siteContactField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
        	if (isNowFocused) {
            	resetOrderedByElements();
        	}
    	});


    	siteContactPhoneField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
        	if (isNowFocused) {
            	resetOrderedByElements();
        	}
    	});


    	POField.textProperty().addListener((observable, oldValue, newValue) -> {
        	// Check if there's text in the field and update the style accordingly
        	if (newValue.isEmpty()) {
            	POField.getStyleClass().remove("has-text");
            	POField.getStyleClass().add("empty-unfocused");
        	} else {
            	POField.getStyleClass().remove("empty-unfocused");
            	if (!POField.getStyleClass().contains("has-text")) {
                	POField.getStyleClass().add("has-text");
            	}
        	}
    	});


    	// Handle focus events for addressField
    	POField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
        	if (isNowFocused) {
            	POField.getStyleClass().remove("empty-unfocused"); // Remove empty style when focused
            	resetOrderedByElements();
        	} else if (POField.getText().isEmpty()) {
            	POField.getStyleClass().add("empty-unfocused"); // Add empty style when focus is lost
        	}
    	});


    	createCustomTooltip(notesButton, 38, 10, notesTooltip);
		createCustomTooltip(printButton, 38, 10, printTooltip);

    	setupTextFieldListeners(notesField, notesButton, notesLabel);


    	// Initialize TableView columns
		DateTimeFormatter inputFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		DateTimeFormatter outputFormat = DateTimeFormatter.ofPattern("M/dd");
		
		rentalDateColumn.setCellValueFactory(cellData -> {
			String rawDate = cellData.getValue().orderDateProperty().get();
			try {
				LocalDate date = LocalDate.parse(rawDate, inputFormat);
				String formatted = outputFormat.format(date);
				return new SimpleStringProperty(formatted);
			} catch (Exception e) {
				// fallback if date is null or malformed
				return new SimpleStringProperty("");
			}
		});
		
		

		
		liftTypeColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
    	deliveryTimeColumn.setCellValueFactory(cellData -> cellData.getValue().deliveryTimeProperty()); // Bind delivery time

		expandColumn.setCellValueFactory(cellData ->
			cellData.getValue().rentalItemIdProperty().asObject()
		);
		
		expandColumn.setCellFactory(col -> new TableCell<>() {
			private final Button expandButton;

			{
				ImageView imageView;

				URL imageUrl = getClass().getResource("/images/small-expand.png");
				if (imageUrl == null) {
					System.err.println("❌ ERROR: expand.png not found at /images/expand.png");
					imageView = new ImageView(); // fallback
				} else {
					Image image = new Image(imageUrl.toExternalForm(), 11, 11, true, true);
					imageView = new ImageView(image);
				}

				expandButton = new Button();
				expandButton.setGraphic(imageView);

				// 🧼 Inline style to avoid row-height inflation
				expandButton.setStyle(
					"-fx-background-color: transparent;" +
					"-fx-border-color: transparent;" +
					"-fx-padding: 0;" +               // Tightest possible padding
					"-fx-background-insets: 0;" +     // Removes default button inset
					"-fx-focus-color: transparent;" +
					"-fx-faint-focus-color: transparent;"
				);

				expandButton.setOnAction(event -> {
					Rental rental = getTableView().getItems().get(getIndex());
					handleExpandClick(rental);
				});

				expandButton.setOnMouseEntered(e -> {
					expandButton.setStyle(
						"-fx-background-color: orange;" +
						"-fx-border-color: transparent;" +
						"-fx-padding: 0;" +
						"-fx-background-insets: 0;" +
						"-fx-focus-color: transparent;" +
						"-fx-faint-focus-color: transparent;"
					);
				});
				
				expandButton.setOnMouseExited(e -> {
					expandButton.setStyle(
						"-fx-background-color: transparent;" +
						"-fx-border-color: transparent;" +
						"-fx-padding: 0;" +
						"-fx-background-insets: 0;" +
						"-fx-focus-color: transparent;" +
						"-fx-faint-focus-color: transparent;"
					);
				});
				


			}

			@Override
			protected void updateItem(Integer item, boolean empty) {
				super.updateItem(item, empty);
				setGraphic(empty ? null : expandButton);
				setContentDisplay(ContentDisplay.GRAPHIC_ONLY); // No text space reserved
				setPadding(Insets.EMPTY); // Ensures cell padding is tight too
			}
		});

	


		// Bind the rentalsList to the TableView
		scheduledRentalsTableView.setItems(rentalsList);

		// 🆕 Check if the static list has any previously saved rows
		ObservableList<Rental> previousList = MaxReachPro.getScheduleDeliveryTableViewSet();
		if (previousList != null && !previousList.isEmpty()) {
			rentalsList.setAll(previousList); // Replace current list with saved list
			scheduledRentalsTableView.setVisible(true);
			tableViewTitle.setVisible(true);
			scheduledCounter = rentalsList.size();
			printButton.setVisible(true);
			adjustTableViewHeight();          // Adjust table height accordingly
		} else {
			// Adjust the TableView height to fit one empty row initially
			adjustTableViewHeight(); // Set initial height of the TableView
			printButton.setVisible(false);
		}

    	// Hide the status label initially
    	statusLabel.setVisible(false);


    	// Start rotating highlight for lift type toggle buttons

		initializeGrid();


	}


	private void autoFillCustomerName(String input) {
    	if (input.isEmpty()) {
        	selectedCustomer = null; // Clear the selected customer if the input is empty
        	orderedByBox.getItems().clear(); // Clear ComboBoxes
        	siteContactBox.getItems().clear();
        	return;
    	}




    	ObservableList<String> matchingNames = FXCollections.observableArrayList();




    	for (Customer customer : customers) {
        	if (customer.getName().toLowerCase().contains(input.toLowerCase())) {
            	matchingNames.add(customer.getName());
        	}
    	}




    	if (matchingNames.size() == 1) {
        	// Auto-fill if there's a single match
        	String matchedName = matchingNames.get(0);
        	customerNameField.setText(matchedName);
        	loadSelectedCustomerByName(matchedName); // Load customer object




        	// Populate ComboBoxes with contacts for the matched customer
        	populateComboBoxesForCustomer(selectedCustomer.getCustomerId());
        	orderedByField.clear();
        	orderedByPhoneField.clear();
        	siteContactField.clear();
        	siteContactPhoneField.clear();
        	notesField.clear();
        	notesButton.getStyleClass().remove("schedule-delivery-button-has-value");


        	// Remove focus from the text field after auto-complete
        	customerNameField.getParent().requestFocus();
    	}
	}




	// Method to load the selected customer based on their name
	private void loadSelectedCustomerByName(String customerName) {
    	for (Customer customer : customers) {
        	if (customer.getName().equals(customerName)) {
            	selectedCustomer = customer;  // Set the selected customer
            	orderedByField.clear();
            	orderedByPhoneField.clear();
            	siteContactField.clear();
            	siteContactPhoneField.clear();
            	break;
        	}
    	}
	}


	// Method to populate ComboBoxes with contacts for the selected customer
	private void populateComboBoxesForCustomer(String customerId) {
		ObservableList<String> orderingContacts = FXCollections.observableArrayList();
		ObservableList<String> siteContacts = FXCollections.observableArrayList();

		// OBFUSCATE_OFF
		String query = "SELECT first_name, phone_number, is_ordering_contact, is_site_contact, contact_id FROM contacts WHERE customer_id = ?";
		// OBFUSCATE_ON
		try (Connection connection = DriverManager.getConnection(Config.DB_URL, Config.DB_USR, Config.DB_PSWD);
			PreparedStatement preparedStatement = connection.prepareStatement(query)) {

			preparedStatement.setString(1, customerId);
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				while (resultSet.next()) {
					String contactName = resultSet.getString("first_name");
					String phoneNumber = resultSet.getString("phone_number");
					boolean isOrderingContact = resultSet.getBoolean("is_ordering_contact");
					boolean isSiteContact = resultSet.getBoolean("is_site_contact");
					String contactId = resultSet.getString("contact_id");

					// Add to ComboBoxes and store phone numbers in the maps
					if (isOrderingContact) {
						orderingContacts.add(contactName);
						for (Customer customer : customers) {
							if (customer.getCustomerId().equals(customerId)) {
								customer.addOrderingContact(contactName, phoneNumber, contactId);
								break;
							}
						}
					}
					if (isSiteContact) {
						siteContacts.add(contactName);
						for (Customer customer : customers) {
							if (customer.getCustomerId().equals(customerId)) {
								customer.addSiteContact(contactName, phoneNumber, contactId);
								break;
							}
						}
					}
				}
			}

			// ✅ Alphabetize the lists
			orderingContacts.sort(String::compareToIgnoreCase);
			siteContacts.sort(String::compareToIgnoreCase);

			// Populate the ComboBoxes
			orderedByBox.setItems(orderingContacts);
			siteContactBox.setItems(siteContacts);

			// Add listeners to handle selection events
			orderedByBox.setOnAction(event -> handleContactSelection(orderedByBox, true)); // true for ordering
			siteContactBox.setOnAction(event -> handleContactSelection(siteContactBox, false)); // false for site

		} catch (SQLException e) {
			System.err.println("Error fetching contacts: " + e.getMessage());
			e.printStackTrace();
		}
	}





	// Method to handle contact selection and populate fields
	private void handleContactSelection(ComboBox<String> comboBox, boolean isOrderingContact) {
    	String selectedContactName = comboBox.getValue();


    	// Check if the selected contact name is not null
    	if (selectedContactName != null) {


        	Map<String, String> phoneNumbers = isOrderingContact
                	? selectedCustomer.getOrderingContactsPhoneNumbers()
                	: selectedCustomer.getSiteContactsPhoneNumbers();
        	Map<String, String> contactIds = isOrderingContact
                	? selectedCustomer.getOrderingContactsIds()
                	: selectedCustomer.getSiteContactsIds();


        	if (isOrderingContact) {
            	orderedByField.setText(selectedContactName);  // Name
            	orderedByPhoneField.setText(phoneNumbers.get(selectedContactName));	// Phone number
            	selectedOrderingContactId = contactIds.get(selectedContactName); // Contact ID
        	} else {
            	siteContactField.setText(selectedContactName);  // Name
            	siteContactPhoneField.setText(phoneNumbers.get(selectedContactName)); 	// Phone number
            	selectedSiteContactId = contactIds.get(selectedContactName); // Contact ID
        	}


    	}


	}


	private void handleCustomerNameFieldUnfocus() {
    	String input = customerNameField.getText();
    	boolean isValidCustomer = false;




    	if (!input.isEmpty()) {
        	for (Customer customer : customers) {
            	if (customer.getName().equalsIgnoreCase(input)) {
                	isValidCustomer = true;
                	break;
            	}
        	}




        	if (!isValidCustomer && input != "") {
            	scheduleDeliveryButton.setText("Add New Customer: " + input);
        	} else {
            	scheduleDeliveryButton.setText("Schedule Delivery");
        	}
    	}
	}






	private boolean isCustomerValid(String customerId) {
    	System.out.println("checking customer " + customerId); // Check if any Customer in the customers list has a matching customerId
    	for (Customer customer : customers) {
        	if (customer.getCustomerId().equals(customerId)) {
            	return true; // Found a matching customerId
        	}
    	}
    	return false; // No matching customerId found
	}


	private boolean isPhoneNumberValid(String phoneNumber){
    	phoneNumber = phoneNumber.trim(); // Remove leading and trailing whitespace
    	String digitsOnly = phoneNumber.replaceAll("[^0-9]", ""); // Remove all non-numeric characters
    	return digitsOnly.length() == 10;
	}


	private void setDefaultRentalDate(String suffix) {
    	// Set rental date to the next weekday (Mon-Fri)
    	LocalDate today = LocalDate.now();
    	LocalDate nextWeekday = today.plusDays(1);
    	while (nextWeekday.getDayOfWeek().getValue() > 5) { // Skip Saturday and Sunday
        	nextWeekday = nextWeekday.plusDays(1);
    	}


    	if (suffix.equals("")) {
        	updateWeekdayToggleButtons(nextWeekday, suffix, weeksRowTilePane);
    	} else {
        	updateWeekdayToggleButtons(nextWeekday, suffix, weeksRowTilePaneAT);
        	weeksRowToggleGroupAT.selectToggle(null);
        	selectedCallOffDate = null;
    	}
	}


	// Method to update the ToggleButtons based on the next weekday
	private void updateWeekdayToggleButtons(LocalDate day, String suffix, TilePane tilePane) {
    	//System.out.println("update weekday toggles called wit day: " +
            	//day + ", suffix: " + suffix);
    	LocalDate startOfWeek = day.with(java.time.temporal.ChronoField.DAY_OF_WEEK, 1); // Monday


    	// Update the weekday buttons with the corresponding dates
    	ToggleButton monButton = (ToggleButton) tilePane.lookup("#monButton" + suffix);
    	ToggleButton tueButton = (ToggleButton) tilePane.lookup("#tueButton" + suffix);
    	ToggleButton wedButton = (ToggleButton) tilePane.lookup("#wedButton" + suffix);
    	ToggleButton thuButton = (ToggleButton) tilePane.lookup("#thuButton" + suffix);
    	ToggleButton friButton = (ToggleButton) tilePane.lookup("#friButton" + suffix);


    	// Create an array to hold the buttons and their respective dates
    	ToggleButton[] buttons = {monButton, tueButton, wedButton, thuButton, friButton};


    	for (int i = 0; i < buttons.length; i++) {
        	LocalDate buttonDate = startOfWeek.plusDays(i); // Calculate the date for this button
        	buttons[i].setText(buttonDate.format(DateTimeFormatter.ofPattern("M/d"))); // Set text to M/d format


        	// Select the button representing the nextWeekday
        	if (buttonDate.equals(day)) {
            	buttons[i].setSelected(true);
            	if (suffix.equals("AT")) {
                	selectedCallOffDate = buttons[i];
            	}
        	} else {
            	buttons[i].setSelected(false);
        	}


        	if (suffix.isEmpty()) {
            	buttons[i].focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                	if (isNowFocused) {
                    	resetOrderedByElements();
                	}
            	});
        	}
    	}




	}


	private void openCalendar(DatePicker picker) {
    	if (picker==datePicker) {
        	picker.show();
        	isCalendarExpanded.set(true);
        	picker.requestFocus(); // Focus on the DatePicker


        	// When a date is selected, update the hidden buttons and reset the DatePicker
        	picker.setOnAction(event -> {
            	LocalDate selectedDate = picker.getValue();
            	dateSelected.set(true);
            	updateWeekdayToggleButtons(selectedDate, "", weeksRowTilePane);
            	isCalendarExpanded.set(false);


        	});


        	// Optionally hide the DatePicker if the user clicks outside or cancels the operation
        	picker.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            	if (!isNowFocused) {
                	if (!dateSelected.get()) {
                    	closeCalendar(picker);
                	}
            	}
        	});
    	} else {
        	picker.show();
        	isATCalendarExpanded.set(true);
        	picker.requestFocus(); // Focus on the DatePicker


        	// When a date is selected, update the hidden buttons and reset the DatePicker
        	picker.setOnAction(event -> {
            	LocalDate selectedDate = picker.getValue();
            	dateSelectedAT.set(true);
            	updateWeekdayToggleButtons(selectedDate, "AT", weeksRowTilePaneAT);
            	isATCalendarExpanded.set(false);


        	});


        	// Optionally hide the DatePicker if the user clicks outside or cancels the operation
        	picker.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            	if (!isNowFocused) {
                	if (!dateSelectedAT.get()) {
                    	closeCalendar(picker);
                    	autoTermButton.setOnAction(event -> {
                        	handleOpenCalendarAT();
                        	autoTermButton.setOnAction(e -> {
                            	closeCalendar(datePickerAT);
                        	});
                    	});
                	}
            	}
        	});
    	}
	}


	/*
	public void openCalendar(DatePicker picker) {
    	AtomicBoolean expandedBoolean;
    	Rectangle cover;
    	ToggleGroup group;
    	AtomicBoolean selection;
    	TilePane pane;
    	if (picker==datePicker) {
        	expandedBoolean = isCalendarExpanded;
        	cover = calendarCover;
        	group = weeksRowToggleGroup;
        	selection = dateSelected;
        	pane = weeksRowTilePane;
    	} else {
        	expandedBoolean = isATCalendarExpanded;
        	cover = calendarCoverAT;
        	group = weeksRowToggleGroupAT;
        	selection = dateSelectedAT;
        	pane = weeksRowTilePaneAT;
    	}


    	picker.show();
    	expandedBoolean.set(true);
    	// Hide the calendarCover when the DatePicker is opened
    	calendarCover.setVisible(false);
    	for (Toggle toggle : weeksRowToggleGroup.getToggles()) {
        	if (toggle instanceof ToggleButton) {
            	((ToggleButton) toggle).setVisible(false); // Hide each ToggleButton
        	}
    	}


    	// Show the DatePicker when the calendar button is clicked
    	//dateSelected = false;
    	//datePicker.setVisible(true);
    	picker.requestFocus(); // Focus on the DatePicker


    	// When a date is selected, update the hidden buttons and reset the DatePicker
    	picker.setOnAction(event -> {
        	LocalDate selectedDate = picker.getValue();
            	selection.set(true);
            	updateWeekdayToggleButtons(selectedDate, "", pane);
            	expandedBoolean.set(false);


    	});


    	// Optionally hide the DatePicker if the user clicks outside or cancels the operation
    	picker.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
        	if (!isNowFocused) {
            	if (!selection.get()) {
                	closeCalendar(picker);
            	}
        	}
    	});
	}
*/


	private void closeCalendar(DatePicker picker) {
    	AtomicBoolean expandedBoolean;
    	Rectangle cover;
    	ToggleGroup group;
    	if (picker==datePicker) {
        	expandedBoolean = isCalendarExpanded;
        	cover = calendarCover;
        	group = weeksRowToggleGroup;
    	} else {
        	expandedBoolean = isATCalendarExpanded;
        	cover = calendarCoverAT;
        	group = weeksRowToggleGroupAT;
        	autoTermButton.setOnAction(e3 -> {
            	handleAutoTerm();
        	});
    	}
    	expandedBoolean.set(false);
    	cover.setVisible(true);
    	for (Toggle toggle : group.getToggles()) {
        	if (toggle instanceof ToggleButton) {
            	((ToggleButton) toggle).setVisible(true); // Hide each ToggleButton
        	}
    	}
	}




	private void startHighlightRotation() {
    	for (javafx.scene.Node node : liftTypeTilePane.getChildren()) {
        	node.getStyleClass().removeAll("lift-type-button-stopped");
        	node.getStyleClass().add("lift-type-button-rotating");
    	}
    	isRotating = true; // Set isRotating to true


    	rotationTimeline = new Timeline();
    	KeyFrame keyFrame = new KeyFrame(Duration.seconds(1.5), event -> {
        	Toggle selectedToggle = liftTypeToggleGroup.getSelectedToggle();
        	Toggle nextToggle = getNextToggle(selectedToggle);
        	liftTypeToggleGroup.selectToggle(nextToggle);
    	});


    	rotationTimeline.getKeyFrames().add(keyFrame);
    	rotationTimeline.setCycleCount(Timeline.INDEFINITE); // Set to repeat indefinitely
    	rotationTimeline.play();
	}






	private Toggle getNextToggle(Toggle currentToggle) {
    	Toggle nextToggle = null;


    	if (currentToggle == null) {
        	return liftTypeToggleGroup.getToggles().get(0); // If none is selected, return the first
    	}


    	int currentIndex = liftTypeToggleGroup.getToggles().indexOf(currentToggle);
    	int nextIndex = (currentIndex + 1) % liftTypeToggleGroup.getToggles().size(); // Wrap around
    	nextToggle = liftTypeToggleGroup.getToggles().get(nextIndex);


    	return nextToggle;
	}


	@FXML
	private void handlePlus() {
    	liftCount++;
    	ToggleButton selectedLiftTypeButton = (ToggleButton) liftTypeToggleGroup.getSelectedToggle();
    	String liftType = selectedLiftTypeButton.getText();
    	addedLifts.add(liftType);
    	liftCountLabel.setVisible(true);
    	xButton.setVisible(true);
    	liftCountLabel.setText(String.valueOf(liftCount));
    	prepareLiftTypeButtons();
    	plusButton.setVisible(false);
    	liftTypeCounts.put(selectedLiftTypeButton, liftTypeCounts.getOrDefault(selectedLiftTypeButton, 0) + 1);
    	updateCountIndicator(selectedLiftTypeButton);
	}


	private void updateCountIndicator(ToggleButton button) {
    	// Find the corresponding label for the button
    	String buttonText = button.getText();
    	Label countLabel = null;




    	switch (buttonText) {
        	case "12' Mast":
            	countLabel = label12m;
            	break;
        	case "19' Slim":
            	countLabel = label19s;
            	break;
        	case "26' Slim":
            	countLabel = label26s;
            	break;
        	case "26'":
            	countLabel = label26;
            	break;
        	case "32'":
            	countLabel = label32;
            	break;
        	case "40'":
            	countLabel = label40;
            	break;
        	case "33' RT":
            	countLabel = label33rt;
            	break;
        	case "45' Boom":
            	countLabel = label45b;
            	break;
    	}




    	if (countLabel != null) {
        	int count = liftTypeCounts.getOrDefault(button, 0);


        	// Set the count in the circle indicator
        	countLabel.setText(String.valueOf(count));


        	// If the count is greater than 0, make the label visible
        	if (count > 0) {
            	countLabel.setVisible(true);
        	} else {
            	// Otherwise, hide the label
            	countLabel.setVisible(false);
        	}
    	}
	}


	@FXML
	private void handleX() {
    	// Ensure there's something to remove
    	if (addedLifts.isEmpty()) {
        	return;
    	}




    	// Decrease the total lift count
    	liftCount--;
    	liftCountLabel.setText(String.valueOf(liftCount));




    	// Get the last added lift type and remove it from the list
    	String removedLiftType = addedLifts.remove(addedLifts.size() - 1);




    	// Find the ToggleButton associated with the removed lift type
    	ToggleButton removedLiftButton = getLiftButtonByText(removedLiftType);




    	// Decrement the count for the removed lift type
    	if (removedLiftButton != null) {
        	liftTypeCounts.put(removedLiftButton, liftTypeCounts.getOrDefault(removedLiftButton, 0) - 1);
        	// Update the count indicator for that lift type
        	updateCountIndicator(removedLiftButton);
    	}




    	// Hide the lift count label if there are no lifts remaining
    	if (liftCount == 0) {
        	liftCountLabel.setVisible(false);
        	xButton.setVisible(false);
    	}




    	// If no more lifts are selected, hide the plus button
    	if (addedLifts.isEmpty()) {
        	plusButton.setVisible(true); // Show the plus button again for new additions
    	}




    	// If rotating, hide the plus button (if it's not already hidden)
    	if (isRotating) {
        	plusButton.setVisible(false);
    	}
	}




	// Helper method to find the corresponding ToggleButton by lift type text
	private ToggleButton getLiftButtonByText(String liftType) {
    	switch (liftType) {
        	case "12' Mast":
            	return twelveMastButton;
        	case "19' Slim":
            	return nineteenSlimButton;
        	case "26' Slim":
            	return twentySixSlimButton;
        	case "26'":
            	return twentySixButton;
        	case "32'":
            	return thirtyTwoButton;
        	case "40'":
            	return fortyButton;
        	case "33' RT":
            	return thirtyThreeRTButton;
        	case "45' Boom":
            	return fortyFiveBoomButton;
        	default:
            	return null;
    	}
	}




	private void updateSuggestions(String input) {
    	System.out.println("updateSuggestions called");
    	addressSuggestions.clear(); // Clear previous suggestions


    	// Only make the request if the input is sufficiently long
    	if (input.length() < 3) {
        	suggestionsBox.getItems().setAll(addressSuggestions);
        	//	suggestionsBox.setVisible(false);
        	return;
    	}


    	String apiKey = Config.GOOGLE_KEY; // Replace with your actual Google Places API key
    	double latitude = 39.7392;  // Example: Denver, CO latitude
    	double longitude = -104.9903;  // Example: Denver, CO longitude
    	int radius = 50000;  // Bias within 50 km radius


    	String url = "https://maps.googleapis.com/maps/api/place/autocomplete/json?input=" +
            	URLEncoder.encode(input, StandardCharsets.UTF_8) +
            	"&key=" + apiKey +
            	"&location=" + latitude + "," + longitude +
            	"&radius=" + radius;




    	Request request = new Request.Builder().url(url).build();


    	client.newCall(request).enqueue(new Callback() {
        	@Override
        	public void onFailure(Call call, IOException e) {
            	e.printStackTrace();
            	//	Platform.runLater(() -> suggestionsBox.setVisible(false)); // Hide suggestions on failure
        	}


        	@Override
        	public void onResponse(Call call, Response response) throws IOException {
            	if (response.isSuccessful()) {
                	String jsonData = response.body().string();
                	// Parse the JSON response to extract suggestions
                	parseAddressSuggestions(jsonData);


                	// Update the suggestions box on the JavaFX Application Thread
                	Platform.runLater(() -> {
                    	suggestionsBox.getItems().setAll(addressSuggestions);
                    	if (addressTypeCounter > 3) {
                        	suggestionsBox.setVisible(!addressSuggestions.isEmpty()); // Show suggestions if not empty
                        	System.out.println("Suggestions Box visibility set to " + suggestionsBox.isVisible());
                        	System.out.println("Number of suggestions: " + addressSuggestions.size());
                    	}
                	});
            	} else {
                	//   Platform.runLater(() -> suggestionsBox.setVisible(false)); // Hide suggestions on error
            	}
        	}
    	});
	}


	private void parseAddressSuggestions(String jsonData) {
    	try {
        	JSONObject jsonObject = new JSONObject(jsonData);
        	JSONArray predictions = jsonObject.getJSONArray("predictions");




        	// Map of replacements (longer terms to shorter abbreviations)
        	Map<String, String> replacements = new HashMap<>();
        	replacements.put("Street", "St");
        	replacements.put("Avenue", "Ave");
        	replacements.put("Boulevard", "Blvd");
        	replacements.put("Road", "Rd");
        	replacements.put("Lane", "Ln");
        	replacements.put("Court", "Ct");
        	replacements.put("Place", "Pl");
        	replacements.put("Square", "Sq");
        	replacements.put("Drive", "Dr");
        	replacements.put("Parkway", "Pkwy");
        	replacements.put("Highway", "Hwy");
        	replacements.put("Trail", "Trl");
        	replacements.put("Way", "Way");
        	replacements.put("Terrace", "Ter");
        	replacements.put("Expressway", "Exp");
        	replacements.put("Center", "Ctr");
        	replacements.put("County", "Cty");
        	replacements.put("North", "N");
        	replacements.put("South", "S");
        	replacements.put("East", "E");
        	replacements.put("West", "W");




        	for (int i = 0; i < predictions.length(); i++) {
            	JSONObject prediction = predictions.getJSONObject(i);
            	String suggestion = prediction.getString("description");




            	// Remove ", USA" if it exists
            	if (suggestion.endsWith(", CO, USA")) {
                	suggestion = suggestion.substring(0, suggestion.length() - 9); // Remove last 5 characters
            	}




            	// Replace long strings with their abbreviated forms
            	for (Map.Entry<String, String> entry : replacements.entrySet()) {
                	suggestion = suggestion.replace(entry.getKey(), entry.getValue());
            	}




            	addressSuggestions.add(suggestion); // Add modified suggestion to the list
        	}
    	} catch (JSONException e) {
        	e.printStackTrace();
    	}
	}


	private String formatSelectedSuggestion(String selectedSuggestion) {
    	// Remove the state and country from the suggestion
    	String[] parts = selectedSuggestion.split(",");
    	if (parts.length >= 2) {
        	// Return formatted address
        	return parts[0] + ", " + parts[1]; // Keep the first two parts (street, city)
    	}
    	return selectedSuggestion; // Return the original suggestion if no comma found
	}


	// Method to adjust the height of the TableView based on the number of entries
	private void adjustTableViewHeight() {
    	double newHeight = INITIAL_TABLE_HEIGHT + (ROW_HEIGHT * Math.max(0, rentalsList.size()));
    	scheduledRentalsTableView.setPrefHeight(newHeight);
		TranslateTransition buttonTransition = new TranslateTransition(Duration.millis(500), printButton);
		buttonTransition.setToY(newHeight/* - printButton.getLayoutY()*/ - 50);
		buttonTransition.play();
		printButton.setVisible(true);
	}


	@FXML
	private void handlePrint () {

		if (rentalsList.isEmpty()) {
			return;
		}

		String sourceFile = Paths.get(PathConfig.CONTRACTS_DIR, "contract template.pdf").toString();
		List<String> createdPdfFiles = new ArrayList<>();

		for (Rental rental : rentalsList) {
			String outputFile = Paths.get(PathConfig.CONTRACTS_DIR, "contract_" + rental.getRentalItemId() + ".pdf").toString();
			double latitude = rental.getLatitude();
			double longitude = rental.getLongitude();
			String mapPage = getGridNameFromCoords(latitude, longitude);
			System.out.println("Latitude: " + latitude + ", Longitude: " + longitude + ", Map Page: " + mapPage);

			try {
				// Open the source PDF
				PdfDocument pdfDoc = new PdfDocument(new PdfReader(sourceFile), new PdfWriter(outputFile));
				Document document = new Document(pdfDoc);

				// Get page 1 of the PDF
				PdfCanvas canvas = new PdfCanvas(pdfDoc.getPage(1));

				// Add text to specific coordinates
				canvas.beginText();
				canvas.setFontAndSize(com.itextpdf.kernel.font.PdfFontFactory.createFont(), 12);

				String dateString = rental.getDeliveryDate();
				DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("MMM-d");
				TemporalAccessor parsedDate = inputFormatter.parse(dateString);

				int month = parsedDate.get(ChronoField.MONTH_OF_YEAR);
				int day = parsedDate.get(ChronoField.DAY_OF_MONTH);

				LocalDate date = resolveDateWithYear(month, day);

				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM'.' d");
				String formattedDate = date.format(formatter);
				String suffix = getDaySuffix(day);
				String formattedDeliveryDate = formattedDate + suffix;
				canvas.setTextMatrix(435, 711); // Delivery Date
				canvas.showText(formattedDeliveryDate);

				// Additional fields
				canvas.setTextMatrix(440 ,747); // Delivery Time
				canvas.showText("P" + rental.getRentalItemId());
				canvas.setTextMatrix(355, 631); // Address Block One
				canvas.showText(rental.getAddressBlockOne());
				canvas.setTextMatrix(346, 613); // Address Block Two
				canvas.showText(rental.getAddressBlockTwo());
				canvas.setTextMatrix(364, 595); // Address Block Three
				canvas.showText(rental.getAddressBlockThree());
				canvas.setTextMatrix(454, 559);
				canvas.showText(mapPage);

				if (rental.getSiteContactName() != null) {
					canvas.setTextMatrix(371, 577); // Address Block Four
					canvas.showText(rental.getSiteContactName());
					canvas.setTextMatrix(454, 577); // Address Block Five
					canvas.showText(formatPhoneNumber(rental.getSiteContactPhone()));
				}

				if (rental.getOrderedByName() != null) {
					canvas.setTextMatrix(119, 559); // Address Block Four
					canvas.showText(rental.getOrderedByName());
					canvas.setTextMatrix(76, 577); // Address Block Five
					canvas.showText(formatPhoneNumber(rental.getOrderedByPhone()));
				}

				canvas.setTextMatrix(194, 559); // PO Number
				canvas.showText(rental.getPoNumber());
				canvas.setTextMatrix(81, 630); // Name
				canvas.showText(rental.getName());
				canvas.setTextMatrix(43, 652);
				canvas.showText(rental.getNotes());
				canvas.setTextMatrix(99, 481); // Lift Type
				canvas.showText(Config.getAbbreviatedLiftType(rental.getLiftType()));
				canvas.endText();

				// Close the document
				document.close();

				// Track the generated PDF file
				createdPdfFiles.add(outputFile);

			} catch (Exception e) {
				System.out.println("Error creating contract for rental ID " + rental.getRentalItemId() + ": " + e.getMessage());
				e.printStackTrace();
			}
		}

		// Merge individual PDFs into one file
		if (!createdPdfFiles.isEmpty()) {
			String todayDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			String finalOutputFile = Paths.get(PathConfig.CONTRACTS_DIR, "contracts_" + todayDate + ".pdf").toString();

			try {
				PdfDocument finalPdfDoc = new PdfDocument(new PdfWriter(finalOutputFile));
				PdfMerger merger = new PdfMerger(finalPdfDoc);

				for (String pdfFile : createdPdfFiles) {
					PdfDocument docToMerge = new PdfDocument(new PdfReader(pdfFile));
					merger.merge(docToMerge, 1, docToMerge.getNumberOfPages());
					docToMerge.close();
				}

				finalPdfDoc.close();

				File finalPdfFile = new File(finalOutputFile);
				if (Desktop.isDesktopSupported()) {
					Desktop desktop = Desktop.getDesktop();
					if (finalPdfFile.exists()) {
						desktop.open(finalPdfFile); // This opens the PDF with the default viewer
					} else {
						System.out.println("The final PDF file does not exist.");
					}
				}

				// Clean up individual PDFs
				for (String pdfFile : createdPdfFiles) {
					File file = new File(pdfFile);
					if (file.exists() && file.delete()) {
					}
				}
			} catch (Exception e) {
				System.out.println("Error merging PDFs: " + e.getMessage());
				e.printStackTrace();
			}
		} else {
			System.out.println("No contracts were created, so no merge occurred.");
		}
	}

	private Tooltip createCustomTooltip(Button button, double xOffset, double yOffset, Tooltip tooltip) {
    	tooltip.setShowDelay(Duration.ZERO);
    	tooltip.setHideDelay(Duration.ZERO);


    	button.setOnMouseEntered(event -> {
        	// Get the button's screen position
        	double buttonX = button.localToScreen(button.getBoundsInLocal()).getMinX();
        	double buttonY = button.localToScreen(button.getBoundsInLocal()).getMaxY();
        	// Show tooltip with an offset relative to the button
        	tooltip.show(button, buttonX + xOffset, buttonY + yOffset);
    	});


    	button.setOnMouseExited(event -> tooltip.hide());


    	return tooltip;
	}


	@FXML
	private void handleAutoTerm() {
    	toggleDedicatedField(autoTermButton, callOffDateLabel, null, weeksRowTilePaneAT, orderedByLabel,
            	orderedByField, orderedByNumberLabel, orderedByPhoneField, orderedByBox);
    	Image calendarImage = new Image(getClass().getResourceAsStream("/images/calendar.png"));
    	ImageView calendarImageView = new ImageView(calendarImage);
    	autoTermButton.setGraphic(calendarImageView);
    	calendarImageView.setFitWidth(30);
    	calendarImageView.setFitHeight(30);
    	autoTermButton.setOnAction(event -> {
        	handleOpenCalendarAT();
        	autoTermButton.setOnAction(e -> {
            	closeCalendar(datePickerAT);
        	});
    	});
	}


	private void resetOrderedByElements() {
    	if (!orderedByField.isVisible()) {
        	toggleDedicatedField(autoTermButton, callOffDateLabel, null, weeksRowTilePaneAT, orderedByLabel,
                	orderedByField, orderedByNumberLabel, orderedByPhoneField, orderedByBox);
        	Image autoImage = new Image(getClass().getResourceAsStream("/images/auto-term.png"));
        	ImageView autoImageView = new ImageView(autoImage);
        	autoTermButton.setGraphic(autoImageView);
        	autoImageView.setFitWidth(30);
        	autoImageView.setFitHeight(30);
        	autoTermButton.setOnAction(event -> {
            	handleAutoTerm();
        	});
        	if (selectedCallOffDate != null) {
            	autoTermButton.getStyleClass().removeAll(autoTermButton.getStyleClass());
            	autoTermButton.getStyleClass().add("schedule-delivery-button");
            	autoTermButton.getStyleClass().add("schedule-delivery-button-has-value");
        	} else {
            	autoTermButton.getStyleClass().removeAll(autoTermButton.getStyleClass());
            	autoTermButton.getStyleClass().add("schedule-delivery-button");
        	}
    	}


	}


	private void handleOpenCalendarAT() {
    	if (!isATCalendarExpanded.get()) {
        	openCalendar(datePickerAT); // Show the calendar
    	} else {
        	closeCalendar(datePickerAT); // Hide the calendar
    	}
	}


	@FXML
	public void handleOpenCalendar() {
    	if (!isCalendarExpanded.get()) {
        	openCalendar(datePicker); // Show the calendar
    	} else {
        	closeCalendar(datePicker); // Hide the calendar
    	}
    	// Toggle the state
	}

	@FXML
	private void handleNotes(){
    	toggleDedicatedField(notesButton, notesLabel, notesField, null, POLabel, POField, null, null, null);
    	POLabel.setVisible(false);
    	resetOrderedByElements();
	}


	private void toggleDedicatedField(Button button, Label label, TextField textField, TilePane tilePane,
                                  	Label anchorLabel, TextField anchorField, Label anchorLabel2, TextField anchorField2, ComboBox anchorComboBox) {
    	boolean isDedicatedFieldVisible = !anchorField.isVisible();
    	boolean isOrderedByLineToggle = anchorLabel == orderedByLabel;


    	anchorLabel.setVisible(isDedicatedFieldVisible);
    	anchorField.setVisible(isDedicatedFieldVisible);
    	if (anchorLabel2 != null) {
        	anchorLabel2.setVisible(isDedicatedFieldVisible);
        	anchorField2.setVisible(isDedicatedFieldVisible);
        	anchorComboBox.setVisible(isDedicatedFieldVisible);
    	}


    	label.setVisible(!isDedicatedFieldVisible);


    	if (selectedCallOffDate != null) {
        	button.getStyleClass().add("schedule-delivery-button-has-value");
    	} else {
        	button.getStyleClass().remove("schedule-delivery-button-has-value");
    	}


    	if (isOrderedByLineToggle) {
        	weeksRowTilePaneAT.setVisible(!isDedicatedFieldVisible);






        	if (isDedicatedFieldVisible) {


        	} else {


        	}


    	} else {
        	notesButton.setVisible(isDedicatedFieldVisible && button != notesButton);
        	textField.setVisible(!isDedicatedFieldVisible);


        	if (!isDedicatedFieldVisible) {
            	// button.setLayoutX(14);
            	textField.requestFocus();
            	if (!textField.getText().isEmpty()) {
                	textField.positionCaret(textField.getText().length());
            	}
        	} else {
            	if (!textField.getText().isEmpty()) {
                	button.getStyleClass().add("schedule-delivery-button-has-value");
            	} else {
                	button.getStyleClass().remove("schedule-delivery-button-has-value");
            	}
            	notesButton.setVisible(true);
        	}
    	}
	}


	private void setupTextFieldListeners(TextField textField, Button button, Label label) {
    	textField.setOnAction(e -> toggleDedicatedField(button, label, textField, null, POLabel, POField, null, null, null));
    	textField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
        	if (!isNowFocused) {
            	toggleDedicatedField(button, label, textField, null, POLabel, POField, null, null, null);
        	}
    	});
	}


	@FXML
	public void handleScheduleDelivery() {
    	if (!scheduleDeliveryButton.getText().equals("Schedule Delivery")){
        	handleAddNewCustomer();
        	return;
    	}

    	ToggleButton selectedLiftTypeButton = (ToggleButton) liftTypeToggleGroup.getSelectedToggle();
    	if (selectedLiftTypeButton == null && liftCount == 0) {
        	statusLabel.setText("Please select a lift type."); // Show error message
        	statusLabel.setTextFill(Color.RED);
        	statusLabel.setVisible(true);
        	return; // Exit the method early
    	}

    	try {
        	String customerId = selectedCustomer.getCustomerId(); // Get customer ID as String
        	String customerName = selectedCustomer.getName(); // Get customer name
        	// Check if the customerId is in the customers list
        	if (!isCustomerValid(customerId)) {
            	statusLabel.setText("Invalid customer ID. Please select a valid customer."); // Show error message
            	statusLabel.setTextFill(Color.RED);
            	statusLabel.setVisible(true);
            	return; // Exit the method early
        	}


        	LocalDate selectedDeliveryDate = getSelectedDate(weeksRowToggleGroup);
        	String dbDeliveryDate = null;
        	String deliveryDate = null;
        	if (selectedDeliveryDate != null) {
            	dbDeliveryDate = selectedDeliveryDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            	deliveryDate = selectedDeliveryDate.format(DateTimeFormatter.ofPattern("MMM-dd", Locale.ENGLISH));
        	}
        	System.out.println("delivery date is: " + deliveryDate);


        	LocalDate coDate;
        	String dbCallOffDate = null;
        	String callOffDate = null;
        	if (selectedCallOffDate != null) {
            	coDate = getSelectedDate(weeksRowToggleGroupAT);
            	dbCallOffDate = coDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            	callOffDate = coDate.format(DateTimeFormatter.ofPattern("MMM-dd", Locale.ENGLISH));
        	}
        	// Check if the custom delivery time is selected
        	String deliveryTime;
        	if (customButton.isSelected()) {
            	String selectedHour = hourComboBox.getSelectionModel().getSelectedItem();
            	//         	String selectedAmPm = ampmComboBox.getSelectionModel().getSelectedItem();
            	if (selectedHour == null /*|| selectedAmPm == null*/) {
                	// Update the status label for input error
                	statusLabel.setText("Please select a custom delivery time."); // Show error message
                	statusLabel.setTextFill(Color.RED); // Set the text color to red
                	statusLabel.setVisible(true); // Make the status label visible
                	return; // Exit the method early
            	}
            	deliveryTime = selectedHour/* + " " + selectedAmPm*/; // Construct delivery time string
        	} else {
            	// Get the selected ToggleButton for delivery time
            	ToggleButton selectedDeliveryTimeButton = (ToggleButton) deliveryTimeToggleGroup.getSelectedToggle();
            	if (selectedDeliveryTimeButton == null) {
                	// Update the status label for input error
                	statusLabel.setText("Please select a delivery time."); // Show error message
                	statusLabel.setTextFill(Color.RED); // Set the text color to red
                	statusLabel.setVisible(true); // Make the status label visible
                	return; // Exit the method early
            	}
            	deliveryTime = selectedDeliveryTimeButton.getText(); // Get the selected delivery time
        	}
        	LocalDate today = LocalDate.now(); // Get the current date
        	String orderDate = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        	String site = siteField.getText();
        	String address = addressField.getText();
        	String addressParts[] = getAddressParts(address); // Format the address for the database
        	String streetAddress = addressParts[0]; // Get the street address
        	String city = addressParts[1]; // Get the city
        	String po = POField.getText();
			String orderedByNameValue = orderedByField.getText();
			String orderedByNumberValue = orderedByPhoneField.getText();
			String siteContactNameValue = siteContactField.getText();
			String siteContactNumberValue = siteContactPhoneField.getText();
			String notes = notesButton.getStyleClass().contains("schedule-delivery-button-has-value") ? notesField.getText() : "";

        	currentCustomerRental = new Rental(customerId, customerName, deliveryDate, deliveryTime,
            	null, "", "Upcoming", po, rentalsList.size() + 1,
             	false, null);
			currentCustomerRental.setAddressBlockOne(site);
        	currentCustomerRental.setAddressBlockTwo(streetAddress);
        	currentCustomerRental.setAddressBlockThree(city);
			currentCustomerRental.setSiteContactName(site);
        	currentCustomerRental.setCallOffDate(callOffDate);
			currentCustomerRental.setOrderedByName(orderedByNameValue);
			currentCustomerRental.setOrderedByPhone(orderedByNumberValue);
			currentCustomerRental.setSiteContactName(siteContactNameValue);
			currentCustomerRental.setSiteContactPhone(siteContactNumberValue);
			currentCustomerRental.setNotes(notes);
			double[] coords = calculateLongAndLat(streetAddress, city);
			currentCustomerRental.setLatitude(coords[0]);
			currentCustomerRental.setLongitude(coords[1]);
			double latitude = currentCustomerRental.getLatitude();
			double longitude = currentCustomerRental.getLongitude();
        	boolean rentalOrderScheduled = false;
        	if (insertRentalOrder(customerId, dbDeliveryDate, orderDate, site, streetAddress, city, po, latitude, longitude)) {
            	rentalOrderScheduled = true;
            	// Update the status label for successful scheduling
            	statusLabel.setText("Rental order scheduled successfully!"); // Show success message
            	statusLabel.setTextFill(Color.GREEN); // Set the text color to green
            	statusLabel.setVisible(true); // Make the status label visible
            	// Add the newly scheduled rental to the rentalsList for this session
            	currentCustomerRental.setRentalOrderId(rentalOrderId); // Set the rental_order_id
            	// Reset fields after successful scheduling
        	} else {
            	statusLabel.setText("Failed to schedule a new Rental Order."); // Show error message
            	statusLabel.setTextFill(Color.RED); // Set the text color to red
            	statusLabel.setVisible(true); // Make the status label visible
            	return;
        	}
        	if (!isRotating){
            	addedLifts.add(selectedLiftTypeButton.getText());
            	liftCount++;
        	}
        	boolean insertedRentalItem = false;
			System.out.println("addedLifts before iterating: " + addedLifts);
        	for (int i = 0; i < liftCount; i++){
            	System.out.println("iteration number: " + i);
            	String liftType =  addedLifts.get(i); // Get the text of the selected button
				System.out.println("adding lift type:" + liftType);
            	int liftId = getLiftIdFromType(liftType);
            	currentCustomerRental.setLiftType(liftType);
            	currentCustomerRental.setLiftId(liftId);
				boolean insertRentalItem = insertRentalItem(currentCustomerRental, rentalOrderId, liftId, currentCustomerRental.getOrderDate(), dbDeliveryDate, dbCallOffDate, deliveryTime, po, orderedByNameValue, orderedByNumberValue, siteContactNameValue, siteContactNumberValue, notes) && rentalOrderScheduled;
            	if (insertRentalItem && rentalOrderScheduled) {
                	// Update the status label for successful scheduling
                	statusLabel.setText("Rental item created successfully!"); // Show success message
                	statusLabel.setTextFill(Color.GREEN); // Set the text color to green
                	statusLabel.setVisible(true); // Make the status label visible

                	rentalsList.add(currentCustomerRental);

                	scheduledCounter++;

                	adjustTableViewHeight(); // Adjust the TableView height after adding a new entry
					
                	// Animate the scissor lift down by decrementing its height
                	//currentHeight -= 50; // Decrease height
                	//MaxReachPro.getScissorLift().animateTransition(currentHeight + ROW_HEIGHT, currentHeight); // Animate the lift

                	adjustTableViewHeight(); // Adjust the TableView height after adding a new entry
                	// Reset fields after successful scheduling
                	insertedRentalItem = true;
            	} else {
                	System.out.println("Failed to insert rental item for liftId: " + liftId); // For debugging
                	// Update the status label for rental failure
					System.out.println("insertedRentalOrder: " + rentalOrderScheduled + " and insertedRentalItem: " + insertRentalItem);
                	statusLabel.setText("Failed to create the rental item. Please try again."); // Show error message
                	statusLabel.setTextFill(Color.RED); // Set the text color to red
                	statusLabel.setVisible(true); // Make the status label visible
            	}
        	}
        	if (insertedRentalItem){
            	MaxReachPro.getScissorLift().animateTransition(this.getTotalHeight());
            	resetFields();
            	scheduledRentalsTableView.setVisible(true);
            	tableViewTitle.setVisible(true);

				new Thread(() -> {
					try {
						HttpRequest request = HttpRequest.newBuilder()
								.uri(URI.create(Config.API_BASE_URL + "/routes/prepare"))
								.GET()
								.build();
			
						HttpClient.newHttpClient()
								.send(request, HttpResponse.BodyHandlers.discarding());
			
						// ✅ only runs AFTER prepare completes
						Platform.runLater(() -> 
							MaxReachPro.getMapController().loadRentalDataFromAPI()
						);
			
					} catch (Exception e) {
						e.printStackTrace();
					}
				}).start();
        	}
    	} catch (Exception e) {
        	// Update the status label for input error
        	statusLabel.setText("Invalid input: " + e.getMessage()); // Show error message
        	statusLabel.setTextFill(Color.RED); // Set the text color to red
        	statusLabel.setVisible(true); // Make the status label visible
        	System.out.println(e.getMessage());
    	}


	}


	private boolean insertRentalOrder(String customerId, String deliveryDate, String orderDate,
									String site, String streetAddress, String city, String po,
									double latitude, double longitude) {
		// OBFUSCATE_OFF
		String query = "INSERT INTO rental_orders (customer_id, delivery_date, order_date, site_name, street_address, city, po_number, latitude, longitude, single_item_order) " +
					"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		// OBFUSCATE_ON
		try (Connection connection = DriverManager.getConnection(Config.DB_URL, Config.DB_USR, Config.DB_PSWD);
			PreparedStatement preparedStatement = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {

			preparedStatement.setString(1, customerId);
			preparedStatement.setString(2, deliveryDate);
			preparedStatement.setString(3, orderDate);
			preparedStatement.setString(4, site);
			preparedStatement.setString(5, streetAddress);
			preparedStatement.setString(6, city);
			preparedStatement.setString(7, po);
			preparedStatement.setDouble(8, latitude);
			preparedStatement.setDouble(9, longitude);
			preparedStatement.setInt(10, liftCount == 0 ? 1 : 0);

			if (preparedStatement.executeUpdate() > 0) {
				// Get the generated rental_order_id
				try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						rentalOrderId = generatedKeys.getInt(1);
						currentCustomerRental.setRentalOrderId(rentalOrderId); // Retrieve the rental_order_id
						currentCustomerRental.setOrderDate(orderDate); // Set the order date
					}
				}
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}


	private boolean insertRentalItem(Rental rental, int localRentalOrderId, int liftId, String orderDate,
		String deliveryDate, String callOffDate, String deliveryTime, String po, String orderedByNameValue, 
		String orderedByNumberValue, String siteContactNameValue, String siteContactNumberValue, 
		String notes) {
		
			int autoTerm = (callOffDate != null) ? 1 : 0;
	
		// First section prepares contact vars

		boolean haveOrderedBy = false, haveSiteContact = false;
		String orderedByContactId = "", siteContactId = "";
	
		if (isPhoneNumberValid(orderedByNumberValue) && !orderedByNameValue.isEmpty()) {
			String cleanedNumber = orderedByNumberValue.replaceAll("\\D", "");
			if (selectedCustomer.isOrderingContactExtant(orderedByNameValue, cleanedNumber)) {
				orderedByContactId = selectedCustomer.getOrderingContactId(orderedByNameValue);
			} else {
				orderedByContactId = insertContactInDB(orderedByNameValue, cleanedNumber, true);
			}
			haveOrderedBy = true;
		}
	
		if (isPhoneNumberValid(siteContactNumberValue) && !siteContactNameValue.isEmpty()) {
			String cleanedNumber = siteContactNumberValue.replaceAll("\\D", "");
			if (selectedCustomer.isSiteContactExtant(siteContactNameValue, cleanedNumber)) {
				siteContactId = selectedCustomer.getSiteContactId(siteContactNameValue);
			} else {
				siteContactId = insertContactInDB(siteContactNameValue, cleanedNumber, false);
			}
			haveSiteContact = true;
		}
	
		System.out.println("Inserting rental item with lift_id: " + liftId);
		// OBFUSCATE_OFF
		String query = "INSERT INTO rental_items (rental_order_id, lift_id, ordered_contact_id, site_contact_id, " +
					   "item_order_date, item_delivery_date, item_call_off_date, auto_term, delivery_time, customer_ref_number, " +
					   "notes, item_status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		System.out.println(query);
		// OBFUSCATE_ON
		try (Connection connection = DriverManager.getConnection(Config.DB_URL, Config.DB_USR, Config.DB_PSWD);
			 PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
	
			preparedStatement.setInt(1, localRentalOrderId);
			preparedStatement.setInt(2, liftId);
			if (haveOrderedBy) {
				preparedStatement.setString(3, orderedByContactId);
			} else {
				preparedStatement.setNull(3, Types.VARCHAR);
			}
			if (haveSiteContact) {
				preparedStatement.setString(4, siteContactId);
			} else {
				preparedStatement.setNull(4, Types.VARCHAR);
			}
			preparedStatement.setString(5, orderDate);
			preparedStatement.setString(6, deliveryDate);
			preparedStatement.setString(7, callOffDate);
			preparedStatement.setInt(8, autoTerm);
			preparedStatement.setString(9, deliveryTime);
			preparedStatement.setString(10, po);
			preparedStatement.setString(11, notes);
			preparedStatement.setString(12, "Upcoming");
	
			// Execute the update
			int rowsAffected = preparedStatement.executeUpdate();
	
			// Retrieve the generated rental_item_id
			try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
				if (generatedKeys.next()) {
					int rentalItemId = generatedKeys.getInt(1);
					rental.setRentalItemId(rentalItemId);  // Set rental_item_id to rental object
					System.out.println("Generated rental_item_id: " + rentalItemId);
				} else {
					System.err.println("Failed to retrieve generated rental_item_id.");
					return false;
				}
			}
	
			// Check if any rows were affected, indicating a successful insert
			return rowsAffected > 0;
	
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	


	private String insertContactInDB(String name, String number, boolean isOrderingContact){
    	System.out.println("Inserting contact in DB with name: " + name + " and number: " + number);
    	String newContactId = null;
		// OBFUSCATE_OFF
    	String query = "INSERT INTO contacts (customer_id, first_name, phone_number, is_ordering_contact, is_site_contact) VALUES (?, ?, ?, ?, ?)";
    	// OBFUSCATE_ON
		try (Connection connection = DriverManager.getConnection(Config.DB_URL, Config.DB_USR, Config.DB_PSWD);
         	PreparedStatement preparedStatement = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {


        	preparedStatement.setString(1, selectedCustomer.getCustomerId());
        	preparedStatement.setString(2, name);
        	preparedStatement.setString(3, number);
        	preparedStatement.setBoolean(4, isOrderingContact);
        	preparedStatement.setBoolean(5, !isOrderingContact);


        	if (preparedStatement.executeUpdate() > 0) {
            	try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                	if (generatedKeys.next()) {
                    	newContactId = generatedKeys.getString(1);
                    	System.out.println("Generated contact_id: " + newContactId);  // For debugging
                	}
            	}
        	}
    	} catch (Exception e) {
        	e.printStackTrace();
    	}
    	return newContactId;
	}


	public LocalDate getSelectedDate (ToggleGroup toggleGroup) {
    	System.out.println("get selected date called");
    	LocalDate date = LocalDate.now();
    	// Ensure a ToggleButton is selected
    	ToggleButton selectedWeekdayButton = (ToggleButton) toggleGroup.getSelectedToggle();
    	System.out.println("Selected weekday button is: " + selectedWeekdayButton);
    	if (selectedWeekdayButton == null) {
        	statusLabel.setText("Please select a weekday."); // Show error message
        	statusLabel.setTextFill(Color.RED);
        	statusLabel.setVisible(true);
        	return null; // Exit the method early
    	}


// Get the text of the selected weekday button (e.g., "12/17")
    	String selectedWeekdayText = selectedWeekdayButton.getText().trim();


// Split the text
    	String[] parts = selectedWeekdayText.split("/");
    	if (parts.length != 2) {
        	statusLabel.setText("Invalid date format. Use MM/DD format.");
        	statusLabel.setTextFill(Color.RED);
        	statusLabel.setVisible(true);
        	return null; // Exit early
    	}


// Parse month and day safely
    	int month, day;
    	try {
        	month = Integer.parseInt(parts[0].trim());
        	day = Integer.parseInt(parts[1].trim());
    	} catch (NumberFormatException e) {
        	statusLabel.setText("Invalid date input. Use numeric MM/DD format.");
        	statusLabel.setTextFill(Color.RED);
        	statusLabel.setVisible(true);
        	return null;
    	}


    	date = resolveDateWithYear(month, day);


// Check if the date falls on a weekend
    	if (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
        	statusLabel.setText("Selected date cannot be on a weekend.");
        	statusLabel.setTextFill(Color.RED);
        	statusLabel.setVisible(true);
        	return null;
    	}
    	return date;
	}


	private void handleExpandClick(Rental rental) {
		
		// Open detail pane, modal, etc.
		MaxReachPro.setRentalForExpanding(rental, rentalsList);
		try {
        	MaxReachPro.loadScene("/fxml/expand.fxml");
        //	GradientAnimator.initialize();
    	} catch (Exception e) {
        	e.printStackTrace();
    	}
	}
	

	public int getLiftIdFromType(String liftType) {
    	return Config.LIFT_TYPE_MAP.getOrDefault(liftType, -1);
	}


	private String[] getAddressParts(String address) {
    	String[] parts = address.split(",");


    	if (parts.length < 2){
        	throw new IllegalArgumentException("address must contain both a street and city separated by a comma");
    	}


    	String street = parts[0].trim();
    	String city = parts[1].trim();


    	return new String[]{street, city};
	}


	public double[] calculateLongAndLat(String streetAddress, String city) {
		if ((streetAddress == null || streetAddress.isEmpty()) && (city == null || city.isEmpty())) {
			System.out.println("Invalid address: Both street address and city are empty.");
			return new double[]{0.0, 0.0}; // Default or error value
		}
	
		String address = streetAddress != null ? streetAddress : "";
		if (city != null && !city.isEmpty()) {
			address += ", " + city + ", Colorado, USA";
		}
	
		System.out.println("Calculating longitude and latitude for address: " + address);
		return fetchCoords(address);
	}
	
	public double[] fetchCoords(String address) {
		System.out.println("Fetching coordinates for address: " + address);
	
		OkHttpClient client = new OkHttpClient();
		String url = "https://maps.googleapis.com/maps/api/geocode/json?address=" + URLEncoder.encode(address, StandardCharsets.UTF_8) + "&key=" + Config.GOOGLE_KEY;
	
		Request request = new Request.Builder().url(url).build();
	
		try (Response response = client.newCall(request).execute()) {
			if (response.isSuccessful() && response.body() != null) {
				String responseData = response.body().string();
				JSONObject json = new JSONObject(responseData);
				String status = json.optString("status", "ERROR");
		
				if ("OK".equals(status)) {
					JSONObject location = json.getJSONArray("results")
							.getJSONObject(0)
							.getJSONObject("geometry")
							.getJSONObject("location");
	
					double lat = location.getDouble("lat");
					double lng = location.getDouble("lng");
	
					System.out.println("Latitude: " + lat + ", Longitude: " + lng);
					return new double[]{lat, lng};
				} else {
					System.out.println("No results found for address: " + address);
				}
			} else {
				System.out.println("Request failed with HTTP status code: " + response.code());
			}
		} catch (IOException e) {
			System.out.println("Request failed: " + e.getMessage());
			e.printStackTrace();
		}
	
		return new double[]{0.0, 0.0}; // Return default values on failure
	}
	

	private void handleAddNewCustomer(){
    	statusLabel.setText("New customer additions currently only supported in Quickbooks");
    	statusLabel.setTextFill(Color.RED);
    	statusLabel.setVisible(true);
	}


	// Utility method to reset fields
	private void resetFields() {
    	customerNameField.clear();
    	orderedByField.clear();
    	orderedByPhoneField.clear();
    	liftTypeToggleGroup.selectToggle(twelveMastButton); // Deselect any selected lift type button
    	Label[] labels = {label12m, label19s, label26s, label26, label32, label40, label33rt, label45b};
    	for (Label label : labels) {
        	label.setVisible(false);
    	}
    	deliveryTimeToggleGroup.selectToggle(deliveryTime8To10Button); // Re-select the "8-10" delivery time toggle button
    	statusLabel.setText(""); // Clear the status label
    	statusLabel.setVisible(false); // Hide the status label
    	hourComboBox.setVisible(false); // Hide hour ComboBox
    	customButton.setSelected(false);
    	closeCalendar(datePicker);
    	closeCalendar(datePickerAT);
    	datePicker.setValue(null);
    	dateSelected.set(false);
    	dateSelectedAT.set(false);
    	setDefaultRentalDate("");
    	setDefaultRentalDate("AT");
    	autoTermButton.getStyleClass().removeAll(autoTermButton.getStyleClass());
    	autoTermButton.getStyleClass().add("schedule-delivery-button");
    	suggestionsBox.setVisible(false);
    	suggestionsBox.getItems().clear();// Restart rotation highlight
    	siteField.clear();
    	addressField.clear();
    	siteContactField.clear();
    	siteContactPhoneField.clear();
    	POField.clear();
    	notesField.clear();
    	notesButton.getStyleClass().remove("schedule-delivery-button-has-value");
    	prepareLiftTypeButtons();
    	liftCount = 0;
    	addedLifts.clear();
    	plusButton.setVisible(false);
    	liftCountLabel.setVisible(false);
    	xButton.setVisible(false);
	}


	private void prepareLiftTypeButtons(){
    	liftTypeToggleGroup.selectToggle(null);
    	for (Toggle toggle : liftTypeToggleGroup.getToggles()) {
        	if (toggle instanceof ToggleButton) {
            	ToggleButton toggleButton = (ToggleButton) toggle;
            	// toggleButton.getStyleClass().remove(toggleButton.getStyleClass());
            	//  toggleButton.setStyle("-fx-background-color: transparent; -fx-text-fill: black; " +
            	//      	"-fx-font-size: 12; -fx-padding: 1 0; -fx-alignment: center;");
            	toggleButton.setOnAction(event ->{
                	liftTypeToggleGroup.selectToggle(toggleButton); // Ensure the selected button is set
                	//  	toggleButton.getStyleClass().add("lift-type-button-stopped");
                	plusButton.setVisible(true);
                	GradientAnimator.stopAllAnimations();
                	for (Toggle tog : liftTypeToggleGroup.getToggles()) {
                    	ToggleButton button = (ToggleButton) tog;
                    	button.getStyleClass().removeAll(button.getStyleClass());
                    	button.getStyleClass().add("lift-type-button-stopped");
                    	/*button.setOnMouseEntered(e -> {
                        	button.getStyleClass().remove("lift-type-button-stopped");
                        	button.getStyleClass().add("lift-type-button-stopped:hover");
                    	});
                    	button.setOnMouseExited(e -> {
                        	button.getStyleClass().remove("lift-type-button-stopped:hover");
                        	button.getStyleClass().add("lift-type-button-stopped");
                    	});*/
                    	button.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                        	if (isNowFocused) {
                            	resetOrderedByElements();
                        	}
                    	});
                	}
            	});
        	}
    	}
    	GradientAnimator.applySequentialGradientAnimationToggles(liftTypeToggleButtons, 2, "lift-type-button-stopped");
	}

	private LocalDate resolveDateWithYear(int month, int day) {
		LocalDate today = LocalDate.now();
		int currentYear = today.getYear();
	
		LocalDate selectedDateThisYear = LocalDate.of(currentYear, month, day);
		LocalDate selectedDateNextYear = selectedDateThisYear.plusYears(1);
	
		return Math.abs(ChronoUnit.DAYS.between(today, selectedDateThisYear)) <= 
			   Math.abs(ChronoUnit.DAYS.between(today, selectedDateNextYear)) 
			   ? selectedDateThisYear 
			   : selectedDateNextYear;
	}
	


	private String getGridNameFromCoords(double latitude, double longitude) {
    	for (GridCell lesserCell : lesserGridCells) {
        	if (lesserCell.contains(latitude, longitude)) {
            	if (lesserCell.isDominant()) {
                	return lesserCell.getCellName();
            	}
            	break;
        	}
    	}


    	for (GridCell greaterCell : greaterGridCells) {
        	if (greaterCell.contains(latitude, longitude)) {
            	return greaterCell.getCellName();
        	}
    	}
    	return "Uncharted";
	}

	private String getDaySuffix(int day) {
    	if (day >= 11 && day <= 13) {
        	return "th";
    	}
    	switch (day % 10) {
        	case 1: return "st";
        	case 2: return "nd";
        	case 3: return "rd";
        	default: return "th";
    	}
	}


	
	private String formatPhoneNumber(String phoneNumber) {
		if (phoneNumber == null) {
			return null;
		}
	
		// Remove all non-digit characters
		String digits = phoneNumber.replaceAll("\\D", "");
	
		// Format only if we have exactly 10 digits
		if (digits.length() == 10) {
			return "(" + digits.substring(0, 3) + ")-" + digits.substring(3, 6) + "-" + digits.substring(6);
		}
	
		// If not 10 digits, return the original input unmodified
		return phoneNumber;
	}
	


	private void initializeGrid() {
    	double latStepGreater = (LAT_MAX - LAT_MIN) / 7;
    	double lonStepGreater = (LON_MAX - LON_MIN) / 13;
    	int cellNameGreater = 720;


    	for (int row = 0; row < 7; row++) {
        	for (int col = 0; col < 13; col++) {
            	double minLat = LAT_MAX - (row + 1) * latStepGreater;
            	double maxLat = LAT_MAX - row * latStepGreater;
            	double minLon = LON_MIN + col * lonStepGreater;
            	double maxLon = LON_MIN + (col + 1) * lonStepGreater;


            	greaterGridCells.add(new GridCell("greater", minLat, maxLat, minLon, maxLon, String.valueOf(cellNameGreater), false));
            	cellNameGreater++;
        	}
        	cellNameGreater += 7;
    	}


    	double latStepLesser = (LAT_MAX - LAT_MIN) / 14;
    	double lonStepLesser = (LON_MAX - LON_MIN) / 26;
    	int cellNameLesser = 61;


    	for (int row = 0; row < 14; row++) {
        	for (int col = 0; col < 26; col++) {
            	double minLat = LAT_MAX - (row + 1) * latStepLesser;
            	double maxLat = LAT_MAX - row * latStepLesser;
            	double minLon = LON_MIN + col * lonStepLesser;
            	double maxLon = LON_MIN + (col + 1) * lonStepLesser;
            	boolean isDominant = dominantLesserCells.contains(String.valueOf(cellNameLesser));
            	lesserGridCells.add(new GridCell("lesser", minLat, maxLat, minLon, maxLon, String.valueOf(cellNameLesser), isDominant));
            	cellNameLesser ++;
        	}
        	cellNameLesser += 4;
    	}


	}

	@Override
	public ObservableList<Rental> getActiveRentalList() {
		return scheduledRentalsTableView.getItems(); // or however your TableView is named
	}
	

	@FXML
	public void handleBack() {
    	try {
        	MaxReachPro.goBack("/fxml/schedule_delivery.fxml");
        	GradientAnimator.initialize();
    	} catch (Exception e) {
        	throw new RuntimeException(e);
    	}
	}


	@Override
	public double getTotalHeight() {
    	if (scheduledCounter == 0){
        	return 358;
    	} else {
        	return 358 - 20 - (ROW_HEIGHT * scheduledCounter);
    	}
	}
}


