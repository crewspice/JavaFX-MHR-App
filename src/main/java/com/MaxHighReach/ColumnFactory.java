package com.MaxHighReach;


import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;


import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;


import static java.lang.Integer.parseInt;


import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.utils.PdfMerger;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Table;


public class ColumnFactory {


	private final TableColumn<Rental, String> customerAndAddressColumn = new TableColumn<>();
	private final TableColumn<Rental, Boolean> statusColumn = new TableColumn<>();
	private final TableColumn<Rental, String> serialNumberColumn = new TableColumn<>();
	private TableColumn<Rental, String> deliveryDateColumn;
	private final TableColumn<Rental, String> deliveryTimeColumn = new TableColumn<>();
	private TableColumn<Rental, String> callOffDateColumn;
	private TableColumn<Rental, String> biModalDateColumn;
	private final TableColumn<Rental, String> driverColumn = new TableColumn<>();
	private final TableColumn<Rental, String> selectColumn = new TableColumn<>();
	private final TableColumn<Rental, String> invoiceColumn = new TableColumn<>();
	private final TableColumn<Rental, String> billColumn = new TableColumn<>();


	private final Button updateRentalButton;
	private TextField serialNumberField;
	private final Button batchButton;
	private String batchSwitcher;
	private final Button secondInProcessButton;
	private String invoiceSecondarySwitcher;
	private String lastActionType = "";
	private final TableView<Rental> dbTableView;
	private Map<String, List<Rental>> groupedRentals = new HashMap<>();
	private Map<String, Integer> driverSequenceMap = new HashMap<>();
	private Set<String> existingDrivers = new HashSet<>();
	private boolean isDriverCacheLoaded = false;
	private boolean shouldShowCheckboxes = false;
	private final ObservableList<String> driverInitials = FXCollections.observableArrayList("A", "J", "I", "B", "JC", "K");
	private String driverComboBoxOpenOrClosed = "";
	private Label globalLiftTypeLabel;
	private static final Map<String, Image> imageCache = new HashMap<>();
	private ActivityController parent;
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




	public ColumnFactory(Button button, TextField textField, TableView<Rental> tableView, Map<String,
        	List<Rental>> rentalsMap, Map<String, Integer> driverMap, Button button2, String buttonString,
                     	Button button3, ActivityController controller) {
    	this.updateRentalButton = button;
    	this.serialNumberField = textField;
    	this.batchButton = button2;
    	this.batchSwitcher = buttonString;
    	this.invoiceSecondarySwitcher = "open-sdk";
    	this.secondInProcessButton = button3;
    	this.dbTableView = tableView;
    	this.groupedRentals = rentalsMap;
    	this.driverSequenceMap = driverMap;
    	this.parent = controller;
    	initializeColumns();
    	initializeGrid();
	}


	// New constructr with only the TableView argument
	public ColumnFactory(TableView<Rental> tableView, Button button) {
    	this.dbTableView = tableView;
    	this.updateRentalButton = button;
    	batchButton = new Button();
    	secondInProcessButton = new Button();
    	this.invoiceSecondarySwitcher = "open-sdk";
    	initializeColumns();
    	initializeGrid();
	}


	// Method to initialize columns and other properties
	private void initializeColumns() {
    	customerAndAddressColumn.setCellFactory(column -> new TableCell<Rental, String>() {
        	private final Label nameLabel = new Label();
        	private final Label addressBlockOneLabel = new Label();
        	private final Label addressBlockTwoLabel = new Label();
        	private final Label addressBlockThreeLabel = new Label();
        	private final VBox contentVBox = new VBox(nameLabel, addressBlockOneLabel, addressBlockTwoLabel, addressBlockThreeLabel);
        	private final Label liftTypeLabel = new Label();
        	private final StackPane overlayPane = new StackPane(contentVBox, liftTypeLabel);
        	private final DropShadow glowEffect = new DropShadow();


        	{
            	contentVBox.setAlignment(Pos.TOP_LEFT);
            	contentVBox.setPadding(new Insets(0));
            	contentVBox.setSpacing(-2);
            	contentVBox.setFillWidth(true);


            	nameLabel.setStyle("-fx-font-weight: bold;");
            	addressBlockOneLabel.setStyle("-fx-font-weight: normal;");
            	addressBlockTwoLabel.setStyle("-fx-font-weight: normal;");
            	addressBlockThreeLabel.setStyle("-fx-font-weight: normal;");


            	contentVBox.setMinHeight(Config.DB_ROW_HEIGHT);
            	contentVBox.setMaxHeight(Config.DB_ROW_HEIGHT);


            	glowEffect.setRadius(10);
            	glowEffect.setSpread(0.5);
            	liftTypeLabel.setEffect(glowEffect);
            	liftTypeLabel.getStyleClass().add("lift-type-in-corner");
            	liftTypeLabel.setStyle("-fx-font-weight: bold;");
            	liftTypeLabel.setFont(Font.font("Patrick Hand"));


            	globalLiftTypeLabel = liftTypeLabel;


            	Timeline glowTimeline = new Timeline(
                    	new KeyFrame(Duration.ZERO,
                            	new KeyValue(glowEffect.radiusProperty(), 10),
                            	new KeyValue(glowEffect.colorProperty(), Color.web(Config.getPrimaryColor(), 0.3))
                    	),
                    	new KeyFrame(Duration.seconds(2),
                            	new KeyValue(glowEffect.radiusProperty(), 10),
                            	new KeyValue(glowEffect.colorProperty(), Color.web(Config.getPrimaryColor(), 0.8))
                    	)
            	);
            	glowTimeline.setCycleCount(Timeline.INDEFINITE);
            	glowTimeline.setAutoReverse(true);
            	glowTimeline.play();


            	StackPane.setAlignment(liftTypeLabel, Pos.BOTTOM_RIGHT);
            	StackPane.setAlignment(contentVBox, Pos.CENTER_LEFT);
            	StackPane.setMargin(liftTypeLabel, new Insets(0, 5, 8, 0));
            	overlayPane.setStyle("-fx-padding: 0 -8 0 -3");
        	}


        	@Override
        	protected void updateItem(String item, boolean empty) {
            	super.updateItem(item, empty);


            	if (empty || getTableRow() == null) {
                	setGraphic(null);
                	setMinHeight(Config.DB_ROW_HEIGHT_EMPTY);
                	setMaxHeight(Config.DB_ROW_HEIGHT_EMPTY);
                	setPrefHeight(Config.DB_ROW_HEIGHT_EMPTY);
            	} else {
                	setMinHeight(Config.DB_ROW_HEIGHT);
                	setMaxHeight(Config.DB_ROW_HEIGHT);
                	setPrefHeight(Config.DB_ROW_HEIGHT);
                	Rental rental = getTableRow().getItem();
                	if (rental != null) {
                    	nameLabel.setText(rental.getName());
                    	addressBlockOneLabel.setText(rental.getAddressBlockOne());
                    	addressBlockTwoLabel.setText(rental.getAddressBlockTwo());
                    	addressBlockThreeLabel.setText(rental.getCity());


                    	String liftType = rental.getShortLiftType();
                    	liftTypeLabel.setText(liftType != null ? liftType : "");
                    	liftTypeLabel.setTranslateY(7);


                    	setGraphic(overlayPane);
                	}
            	}
        	}
    	});


    	customerAndAddressColumn.setPrefWidth(117);


    	statusColumn.setCellValueFactory(cellData -> {
        	Rental rental = cellData.getValue();
        	String status = rental.getStatus();
        	return new SimpleBooleanProperty("Active".equals(status));
    	});


    	statusColumn.setCellFactory(column -> new TableCell<Rental, Boolean>() {
        	@Override
        	protected void updateItem(Boolean item, boolean empty) {
            	super.updateItem(item, empty);


            	if (empty || item == null) {
                	setGraphic(null);
                	setMinHeight(Config.DB_ROW_HEIGHT_EMPTY);
                	setMaxHeight(Config.DB_ROW_HEIGHT_EMPTY);
                	setPrefHeight(Config.DB_ROW_HEIGHT_EMPTY);
            	} else {
                	setMinHeight(Config.DB_ROW_HEIGHT);
                	setMaxHeight(Config.DB_ROW_HEIGHT);
                	setPrefHeight(Config.DB_ROW_HEIGHT);
                	Rental rental = getTableView().getItems().get(getIndex());


                	boolean shouldShow = false;
                	boolean shouldShowExpandIcons = false;


                	if ("calling-off".equals(lastActionType) && "Active".equals(rental.getStatus())) {
                    	shouldShow = true;
                	} else if ("dropping-off".equals(lastActionType) && "Upcoming".equals(rental.getStatus())) {
                    	shouldShow = true;
                	} else if ("picking-up".equals(lastActionType) && "Called Off".equals(rental.getStatus())) {
                    	shouldShow = true;
                	} else if ("composing-invoices".equals(lastActionType) && rental.getNeedsInvoice() ) {
                    	shouldShow = true;
                	} else if ("composing-contracts".equals(lastActionType)) {
                    	shouldShow = true;
                	} else if ("deleting".equals(lastActionType)) {
                    	shouldShow = true;
                	} else if ("expanding".equals(lastActionType)) {
                    	shouldShowExpandIcons = true;
                	}


                	if (shouldShow) {
                    	CheckBox checkBox = new CheckBox();
                    	checkBox.setSelected(rental.isSelected());
                    	checkBox.setOnAction(e -> {
                        	handleSelection(checkBox.isSelected(), getIndex(), lastActionType);
                        	rental.setSelected(checkBox.isSelected());
                    	});


                    	StackPane stackPane = new StackPane(checkBox);
                    	stackPane.setAlignment(Pos.CENTER);
                    	setGraphic(stackPane);
                	} else if (shouldShowExpandIcons) {
                    	Button button = new Button();
                    	String imagePath = "/images/small-expand.png";
                    	ImageView imageView = createImageView(imagePath, 13);
                    	imageView.setFitWidth(13);
                    	button.setGraphic(imageView);
                    	button.getStyleClass().add("expand-button");
                    	int rowIndex = getIndex();
                    	button.setOnAction(event -> handleExpandSelection(rowIndex));
                    	StackPane stackPane = new StackPane(button);
                    	stackPane.setAlignment(Pos.CENTER);
                    	setGraphic(stackPane);
                	} else {
                    	Circle circle = new Circle(8);
                    	String status = rental.getStatus();
                    	Tooltip tooltip = new Tooltip(status);
                    	tooltip.setShowDelay(Duration.ZERO);


                    	if (status.equals("Upcoming")) {
                        	if (MaxReachPro.getUser()[0] == "Sandy Mulberry") {
                            	circle.setFill(Color.web("#F4F471"));
                            	tooltip.setStyle("-fx-background-color: #F4F471; -fx-text-fill: black;");
                        	} else {
                            	circle.setFill(Color.web(Config.getPrimaryColor()));
								int textColorCode = Config.COLOR_TEXT_MAP.getOrDefault(Config.getPrimaryColor(), 0);
								String textFill = "black";
								if (textColorCode == 2) {
									textFill = "white";
								}
								tooltip.setStyle("-fx-background-color: " + Config.getPrimaryColor() + "; -fx-text-fill: " + textFill + ";");
                        	}
                    	} else if (status.equals("Active")) {
                        	circle.setFill(Color.GREEN);
                        	tooltip.setStyle("-fx-background-color: green; -fx-text-fill: white;");
                    	} else if (status.equals("Called Off")) {
                        	circle.setFill(Color.RED);
                        	tooltip.setStyle("-fx-background-color: red; -fx-text-fill: white;");
                    	} else if (status.equals("Picked Up")) {
                        	circle.setFill(Color.BLACK);
                        	tooltip.setStyle("-fx-background-color: black; -fx-text-fill: white;");
                    	}


                    	Tooltip.install(circle, tooltip);


                    	StackPane stackPane = new StackPane(circle);
                    	stackPane.setAlignment(Pos.CENTER);
                    	setGraphic(stackPane);
                	}
            	}
        	}
    	});
    	statusColumn.setPrefWidth(24);
	}


	public TableColumn<Rental, String> getSerialNumberColumn(){
    	serialNumberColumn.setCellValueFactory(new PropertyValueFactory<>("serialNumber"));
    	serialNumberColumn.setCellFactory(column -> new TableCell<Rental, String>() {
        	@Override
        	protected void updateItem(String item, boolean empty) {
            	super.updateItem(item, empty);


            	if (empty || item == null) {
                	setText(null);  // Clear text for empty cells
                	setGraphic(null);  // Clear graphic for empty cells
                	setMinHeight(Config.DB_ROW_HEIGHT_EMPTY);
                	setMaxHeight(Config.DB_ROW_HEIGHT_EMPTY);
                	setPrefHeight(Config.DB_ROW_HEIGHT_EMPTY);
            	} else {
                	setMinHeight(Config.DB_ROW_HEIGHT);
                	setMaxHeight(Config.DB_ROW_HEIGHT);
                	setPrefHeight(Config.DB_ROW_HEIGHT);


                	// Get the serial number from the CustomerRental
                	Rental rental = getTableView().getItems().get(getIndex());
                	if (rental.getLiftId() == 1008) {
                    	rental.setSerialNumber("45");
                	} else if (rental.getLiftId() == 1007) {
                    	rental.setSerialNumber("33");
                	}
                	String serialNumber = rental.getSerialNumber(); // Get serial number




                	// Create a VBox to hold each character as a Label
                	VBox vBox = new VBox();
                	vBox.setAlignment(Pos.CENTER); // Center align the VBox




                	// Create a Label for the '#' character
                	Label hashLabel = new Label("#");
                	hashLabel.setStyle("-fx-font-size: 9.5;"); // Set the style for the hash character
                	vBox.getChildren().add(hashLabel); // Add the hash label to the VBox


                	// Create a Label for each character in the serial number
                	for (int i = 0; i < serialNumber.length(); i++) {
                    	char c = serialNumber.charAt(i);
                    	Label charLabel = new Label(String.valueOf(c));
                    	charLabel.setStyle("-fx-font-size: 9; -fx-padding: 0;"); // Adjust style as needed
                    	vBox.getChildren().add(charLabel); // Add each character label to the VBox
                	}


                	vBox.setSpacing(-5.5); // Set the spacing between characters
                	// Set the VBox as the graphic for the cell
                	setGraphic(vBox); // Set the VBox as the graphic for the cell
            	}
        	}
    	});


    	serialNumberColumn.setPrefWidth(10);
    	return serialNumberColumn;
	}


	private TableColumn<Rental, String> createDateColumn(String name, String modeText) {
    	TableColumn<Rental, String> dateColumn = new TableColumn<>();


    	String propertyName = name == "biModalDate" ? "deliveryDate" : name;




    	dateColumn.setCellValueFactory(new PropertyValueFactory<>(propertyName));


    	dateColumn.setCellFactory(column -> new TableCell<Rental, String>() {
        	private final Label ddLabel = new Label();
        	private final Label m1Label = new Label();
        	private final Label m2Label = new Label();
        	private final Label m3Label = new Label();
        	private final Label modeLabel = new Label(); // Set mode label text
        	private final VBox contentVBox = new VBox(ddLabel, m1Label, m2Label, m3Label, modeLabel);
        	private final StackPane overLayPane = new StackPane(contentVBox);


        	{
            	contentVBox.setAlignment(Pos.CENTER);
            	contentVBox.setPadding(new Insets(0, 5, 2, 5));
            	contentVBox.setSpacing(-7);


            	VBox.setMargin(ddLabel, new Insets(0, 0, 2, 0));
            	VBox.setMargin(modeLabel, new Insets(-2, 0, 0, 0));


            	ddLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12;");
            	m1Label.setStyle("-fx-font-weight: normal; -fx-font-size: 13;");
            	m2Label.setStyle("-fx-font-weight: normal; -fx-font-size: 13;");
            	m3Label.setStyle("-fx-font-weight: normal; -fx-font-size: 13;");
            	modeLabel.setStyle("-fx-font-weight: normal; -fx-font-size: 12; -fx-font-family: 'Lucida Handwriting';");


            	contentVBox.setMinHeight(Config.DB_ROW_HEIGHT);
            	contentVBox.setMaxHeight(Config.DB_ROW_HEIGHT);


            	StackPane.setAlignment(contentVBox, Pos.TOP_CENTER);
            	overLayPane.setStyle("-fx-padding: -1 -7 0 -7");
        	}


        	@Override
        	protected void updateItem(String item, boolean empty) {
            	super.updateItem(item, empty);


            	if (empty || item == null) {
                	setText(null);
                	setGraphic(null);
                	setMinHeight(Config.DB_ROW_HEIGHT_EMPTY);
                	setMaxHeight(Config.DB_ROW_HEIGHT_EMPTY);
                	setPrefHeight(Config.DB_ROW_HEIGHT_EMPTY);
            	} else {
                	setMinHeight(Config.DB_ROW_HEIGHT);
                	setMaxHeight(Config.DB_ROW_HEIGHT);
                	setPrefHeight(Config.DB_ROW_HEIGHT);


                	Rental rental = getTableView().getItems().get(getIndex());
                	String dateValue = null;


                	// Fetch the correct date property dynamically
                	if ("deliveryDate".equals(name)) {
                    	dateValue = rental.getDeliveryDate();
                    	modeLabel.setText("del");
                	} else if ("callOffDate".equals(name)) {
                    	dateValue = rental.getCallOffDate();
                    	modeLabel.setText("end");
                	} else if ("biModalDate".equals(name)) {
                    	if (rental.getStatus().equals("Upcoming") || rental.getStatus().equals("Active")) {
                        	dateValue = rental.getDeliveryDate();
                        	modeLabel.setText("del");
                    	} else {
                        	dateValue = rental.getCallOffDate();
                        	modeLabel.setText("end");
                    	}
                	}


                	if (dateValue != null && dateValue.matches("\\d{4}-\\d{2}-\\d{2}")) {
                    	String[] dateParts = dateValue.split("-");
                    	String day = String.valueOf(Integer.parseInt(dateParts[2])); // Remove leading zero
                    	int month = Integer.parseInt(dateParts[1]);


                    	// Append ordinal suffix for day
                    	String suffix;
                    	int dayInt = Integer.parseInt(day);
                    	if (dayInt >= 11 && dayInt <= 13) {
                        	suffix = "th";
                    	} else if (dayInt % 10 == 1) {
                        	suffix = "st";
                    	} else if (dayInt % 10 == 2) {
                        	suffix = "nd";
                    	} else if (dayInt % 10 == 3) {
                        	suffix = "rd";
                    	} else {
                        	suffix = "th";
                    	}


                    	// Set text for day with suffix
                    	ddLabel.setText(day + suffix);


                    	// Set month labels as three-letter abbreviation
                    	String monthAbbreviation = Month.of(month).getDisplayName(TextStyle.SHORT, Locale.ENGLISH).toUpperCase();
                    	m1Label.setText(String.valueOf(monthAbbreviation.charAt(0)));
                    	m2Label.setText(String.valueOf(monthAbbreviation.charAt(1)));
                    	m3Label.setText(String.valueOf(monthAbbreviation.charAt(2)));


                    	setGraphic(overLayPane);
                	}
            	}
        	}
    	});


    	dateColumn.setPrefWidth(31);
    	return dateColumn;
	}


	public TableColumn<Rental, String> getDeliveryDateColumn() {
    	if (deliveryDateColumn == null) {
        	deliveryDateColumn = createDateColumn("deliveryDate", "del");
    	}
    	return deliveryDateColumn;
	}


	public TableColumn<Rental, String> getCallOffDateColumn() {
    	if (callOffDateColumn == null) {
        	callOffDateColumn = createDateColumn("callOffDate", "end");
    	}
    	return callOffDateColumn;
	}


	public TableColumn<Rental, String> getBiModalDateColumn() {
    	if (biModalDateColumn == null) {
        	biModalDateColumn = createDateColumn("biModalDate", "del");
    	}
    	return biModalDateColumn;
	}


	public TableColumn<Rental, String> getDeliveryTimeColumn(){
    	deliveryTimeColumn.setCellValueFactory(new PropertyValueFactory<>("deliveryTime"));




    	deliveryTimeColumn.setCellFactory(column -> new TableCell<Rental, String>() {
        	private final Label windowBeginLabel = new Label();
        	private final Label windowEndLabel = new Label();
        	private final ImageView clockImage = new ImageView();
        	private final VBox clockContainer = new VBox(clockImage);
        	private final Label divider = new Label();
        	private final VBox contentVBox = new VBox(clockContainer, windowBeginLabel, divider, windowEndLabel);
        	private final StackPane overLayPane = new StackPane(contentVBox);


        	{
            	contentVBox.setSpacing(0); // Minimal spacing overall
            	contentVBox.setAlignment(Pos.CENTER);
            	clockContainer.setAlignment(Pos.CENTER);


            	VBox.setMargin(clockContainer, new Insets(0, 0, 3, 0)); // Space below clockContainer
            	VBox.setMargin(windowBeginLabel, new Insets(0, 0, 0, 0)); // No extra space
            	VBox.setMargin(divider, new Insets(-7, 0, -5, 0)); // Shrink space above and below divider
            	VBox.setMargin(windowEndLabel, new Insets(0, 0, 0, 0)); // No extra space


            	windowBeginLabel.setStyle("-fx-font-size: 10");
            	windowEndLabel.setStyle("-fx-font-size: 10");
            	divider.setStyle("-fx-font-size: 10");
            	overLayPane.setStyle("-fx-padding: -5 -5 -5 -5");
        	}


        	@Override
        	protected void updateItem(String item, boolean empty) {
            	super.updateItem(item, empty);




            	if (empty || item == null) {
                	setText(null);  // Clear text for empty cells
                	setGraphic(null);  // Clear graphic for empty cells
                	setMinHeight(Config.DB_ROW_HEIGHT_EMPTY);
                	setMaxHeight(Config.DB_ROW_HEIGHT_EMPTY);
                	setPrefHeight(Config.DB_ROW_HEIGHT_EMPTY);
            	} else {
                	setMinHeight(Config.DB_ROW_HEIGHT);
                	setMaxHeight(Config.DB_ROW_HEIGHT);
                	setPrefHeight(Config.DB_ROW_HEIGHT);




                	String imagePath = "/images/clock.png";
                	Image image = getCachedImage(imagePath);
                	clockImage.setImage(image);
                	clockImage.setFitHeight(23);
                	clockImage.setPreserveRatio(true);




                	// Get delivery time from the CustomerRental
                	Rental rental = getTableView().getItems().get(getIndex());
                	String time = rental.getDeliveryTime(); // Get delivery time


                	if (time.matches("\\d+-\\d+")) { // Integer-dash-integer format
                    	String[] parts = time.split("-");
                    	int startTime = Integer.parseInt(parts[0]);
                    	int endTime = Integer.parseInt(parts[1]);


                    	windowBeginLabel.setText(formatTimeWithPeriod(startTime));
                    	windowEndLabel.setText(formatTimeWithPeriod(endTime));
                    	divider.setText("\u2014"); // Em-dash
                	} else if (time.matches("\\d+")) { // Single integer format
                    	int startTime = Integer.parseInt(time);
                    	int endTime = (startTime + 2) % 12;
                    	if (endTime == 0) endTime = 12; // Handle 12-hour clock wraparound


                    	windowBeginLabel.setText(formatTimeWithPeriod(startTime));
                    	windowEndLabel.setText(formatTimeWithPeriod(endTime));
                    	divider.setText("\u2014"); // Em-dash
                	} else if ("Any".equalsIgnoreCase(time)) { // "Any" time
                    	windowBeginLabel.setText("Any");
                    	windowEndLabel.setText("Any");
                    	divider.setText("\u2014"); // Em-dash
                	} else if ("ASAP".equalsIgnoreCase(time)) { // "ASAP" time
                    	windowBeginLabel.setText("A S");
                    	windowEndLabel.setText("A P");
                    	divider.setText(""); // Empty string
                	} else { // Handle invalid time formats
                    	windowBeginLabel.setText("N/A");
                    	windowEndLabel.setText("N/A");
                    	divider.setText(""); // Empty string
                	}


                	setGraphic(overLayPane); // Set the VBox as the graphic for the cell
            	}
        	}


        	private String formatTimeWithPeriod(int hour) {
            	if (hour == 12) {
                	return "12pm";
            	} else if (hour > 7) {
                	return hour + "am";
            	} else {
                	return hour + "pm";
            	}
        	}


    	});


    	deliveryTimeColumn.setPrefWidth(23);
    	return deliveryTimeColumn;
	}


	public void setClosedDriverColumn(String type){
    	driverColumn.setCellFactory(column -> new TableCell<Rental, String>() {
        	private final Label topLabel = new Label();
        	private final VBox vBox = new VBox(topLabel);
        	private final VBox vBox2 = new VBox();
        	private final String imagePath = new String("/images/driver-icon.png");
        	private final Image image = getCachedImage(imagePath);
        	private final ImageView imageView = new ImageView(image);
        	{
            	vBox.setSpacing(-2);
            	vBox.setAlignment(Pos.TOP_CENTER);
				

            	vBox2.setAlignment(Pos.BOTTOM_CENTER);


            	imageView.setFitHeight(19);
            	imageView.setFitWidth(19);
				imageView.setTranslateY(9);

				topLabel.setTranslateY(14);
        	}


        	@Override
        	protected void updateItem(String item, boolean empty) {
            	super.updateItem(item, empty);


            	// Check if the cell is empty or the item is null
            	if (empty || item == null) {
                	setText(null);
                	setGraphic(null);
                	setMinHeight(Config.DB_ROW_HEIGHT_EMPTY);
                	setMaxHeight(Config.DB_ROW_HEIGHT_EMPTY);
                	setPrefHeight(Config.DB_ROW_HEIGHT_EMPTY);
            	} else {
                	setMinHeight(Config.DB_ROW_HEIGHT);
                	setMaxHeight(Config.DB_ROW_HEIGHT);
                	setPrefHeight(Config.DB_ROW_HEIGHT);


					// Create or get the label to be displayed
					Rental currentRental = getCurrentRental();
					if ("po-number".equals(type)) {
						topLabel.setText(currentRental.getPoNumber());
					} else if ("interval".equals(type)) {
						topLabel.textProperty().bind(
								Bindings.concat(currentRental.rentalDurationProperty().asString(), " days")
						);
					}

					// Enable line wrapping for the label
					topLabel.setWrapText(true);

					// Set the max width for the label to control when it wraps
					topLabel.setMaxWidth(60); // Adjust this value as needed

					// Keep the text centered both horizontally and vertically
					topLabel.setTextAlignment(TextAlignment.CENTER);
					topLabel.setAlignment(Pos.CENTER);
					topLabel.setLineSpacing(-3);

					// Apply VBox constraints
					vBox.setVgrow(topLabel, Priority.NEVER); // Don't let the label expand vertically

					// Optionally, apply Vgrow to other elements that shouldn't be pushed upward
					vBox.setVgrow(vBox2, Priority.ALWAYS); // Ensure the other elements use the available space properly


                	if ("x".equals(item)) {
                    	displayDriverIconOnly(); // Show only the driver icon when value is "x"
                	} else {
                    	displayDriverWithIcon(item); // Show initials and icon for assigned drivers
                	}
            	}
        	}


        	// Method to display only the driver icon
        	private void displayDriverIconOnly() {
            	try {
                	vBox.getChildren().clear();
                	vBox2.getChildren().clear();
                	vBox2.getChildren().add(imageView);
                	vBox.getChildren().addAll(topLabel, vBox2);
                	setGraphic(vBox);
            	} catch (Exception e) {
                	e.printStackTrace();  // Print the stack trace for debugging
                	setGraphic(null); // Set graphic to null in case of an error
            	}
        	}


        	// Method to display driver initials and icon
        	private void displayDriverWithIcon(String driverInitials) {
            	try {
                	vBox.getChildren().clear();


                	HBox hBox = new HBox();
                	hBox.setSpacing(1);  // Space between initials and the icon
                	hBox.setAlignment(Pos.BOTTOM_CENTER);  // Align icon at the bottom


                	// Create a label for driver initials
                	Label initialsLabel = new Label(driverInitials);
                	initialsLabel.setTextFill(Color.BLACK); // Ensure text is visible
                	initialsLabel.setTranslateX(3);
					initialsLabel.setTranslateY(8);




                	hBox.getChildren().addAll(initialsLabel, imageView);  // Add both to VBox
                	vBox.getChildren().addAll(hBox, topLabel);
                	setGraphic(vBox);
            	} catch (Exception e) {
                	e.printStackTrace(); // Print the stack trace for debugging
                	setText(driverInitials); // Fallback to just initials
                	setTextFill(Color.BLACK); // Ensure text is visible
                	setGraphic(null); // Remove any graphic in case of an error
            	}
        	}


        	private Rental getCurrentRental() {
            	int index = getIndex();
            	return (index >= 0 && index < dbTableView.getItems().size())
                    	? dbTableView.getItems().get(index)
                    	: null;
        	}
    	});
    	driverComboBoxOpenOrClosed = "closed";
	}


	public void setOpenDriverColumn(String type){
    	switch (type) {
        	case "po-number":
            	break;
        	case "invoice":
            	break;
        	case "time-range":
            	break;
        	default:
            	break;
    	}




    	driverColumn.setCellFactory(column -> new TableCell<Rental, String>() {
        	private final ComboBox<String> comboBox = new ComboBox<>();
        	private final Label poLabel = new Label(); // Reuse Label
        	private final VBox vBox = new VBox(comboBox, poLabel);


        	{
            	vBox.setSpacing(2);
            	vBox.setAlignment(Pos.TOP_CENTER);

				comboBox.setTranslateY(7);
				poLabel.setTranslateY(3);

            	comboBox.valueProperty().addListener((obs, oldValue, newValue) -> {
                	if (comboBox.isFocused()) {
                    	System.out.println("User selected: " + newValue);
                    	if (newValue != null && !newValue.equals(oldValue)) {
                        	System.out.println("ComboBox selection changed to: " + newValue);




                        	// Trigger your existing logic here
                        	String selectedDriver = newValue;
                        	Rental currentRental = getCurrentRental();
                        	if (currentRental != null) {
                            	updateGroupedRentals(selectedDriver, currentRental);
                            	commitEdit(selectedDriver);
                            	dbTableView.refresh();
								loadExistingDrivers();
                        	}
                    	}
                	}
            	});


        	}


        	@Override
        	protected void updateItem(String item, boolean empty) {
            	super.updateItem(item, empty);


            	if (empty || getIndex() < 0) {
                	setText(null);
                	setGraphic(null);
                	setPrefHeight(Config.DB_ROW_HEIGHT_EMPTY);
                	return;
            	}


            	Rental currentRental = getCurrentRental();


            	// Avoid redundant updates
            	if (currentRental != null && !item.equals(comboBox.getValue())) {
                	// Update ComboBox only if necessary
                	Set<String> potentialDrivers = calculatePotentialDrivers(currentRental);
                	comboBox.getItems().setAll(potentialDrivers);
                	comboBox.getSelectionModel().select(currentRental.getDriver());


                	// Set poLabel text conditionally
                	if ("po-number".equals(type)) {
                    	poLabel.setText(currentRental.getPoNumber());
                	} else if ("interval".equals(type)) {
                    	poLabel.setText(currentRental.getDeliveryDate() + " to " + currentRental.getCallOffDate());
                	} else {
                    	poLabel.setText(null);
                	}

					// Enable line wrapping for the label
					poLabel.setWrapText(true);

					// Set the max width for the label to control when it wraps
					poLabel.setMaxWidth(60); // Adjust this value as needed

					// Keep the text centered both horizontally and vertically
					poLabel.setTextAlignment(TextAlignment.CENTER);
					poLabel.setAlignment(Pos.CENTER);
					poLabel.setLineSpacing(-3);

					// Apply VBox constraints
					vBox.setVgrow(poLabel, Priority.NEVER); // Don't let the label expand vertically

					// Optionally, apply Vgrow to other elements that shouldn't be pushed upward
					vBox.setVgrow(comboBox, Priority.ALWAYS); // Ensure the other elements use the available space properly



            	}


            	// Set reusable graphic
            	setGraphic(vBox);
            	setPrefHeight(Config.DB_ROW_HEIGHT);
        	}


        	private Rental getCurrentRental() {
            	int index = getIndex();
            	return (index >= 0 && index < dbTableView.getItems().size())
                    	? dbTableView.getItems().get(index)
                    	: null;
        	}


        	@Override
        	public void commitEdit(String newValue) {
            	super.commitEdit(newValue);
            	Rental rental = getCurrentRental();
            	if (rental != null) {
                	rental.setDriver(newValue);
                	rental.setupDriverCompositionalParts();
                	updateDriverInDatabase(rental.getRentalItemId(), newValue);
                	System.out.println("Committed Edit: New Driver: " + newValue);
            	}
        	}
    	});


    	driverComboBoxOpenOrClosed = "open";
	}


	public TableColumn<Rental, String> getDriverColumn(String driverColumnType){
    	driverColumn.setCellValueFactory(new PropertyValueFactory<>("driver"));
    	driverColumn.setPrefWidth(63);  // minimum is 63 for those combo boxes
    	setClosedDriverColumn(driverColumnType);
    	System.out.println("Getting driver column for status: " + driverComboBoxOpenOrClosed);
    	return driverColumn;
	}














	public TableColumn<Rental, String> getSelectColumn(){
    	selectColumn.setCellValueFactory(new PropertyValueFactory<>("deliveryTime"));


    	selectColumn.setCellFactory(column -> new TableCell<Rental, String>() {
        	@Override
        	protected void updateItem(String item, boolean empty) {
            	super.updateItem(item, empty);


            	if (empty || item == null) {
                	setGraphic(null);
                	setMinHeight(Config.DB_ROW_HEIGHT_EMPTY);
                	setMaxHeight(Config.DB_ROW_HEIGHT_EMPTY);
                	setPrefHeight(Config.DB_ROW_HEIGHT_EMPTY);
            	} else {
                	setMinHeight(Config.DB_ROW_HEIGHT);
                	setMaxHeight(Config.DB_ROW_HEIGHT);
                	setPrefHeight(Config.DB_ROW_HEIGHT);
                	Rental rental = getTableView().getItems().get(getIndex());


                	CheckBox checkBox = new CheckBox();
                	checkBox.setSelected(rental.isSelected());
                	checkBox.setOnAction(e -> {
                    	handleSelection(checkBox.isSelected(), getIndex(), "Compose Invoices");
                    	rental.setSelected(checkBox.isSelected());
                	});


                	StackPane stackPane = new StackPane(checkBox);
                	stackPane.setAlignment(Pos.CENTER);
                	setGraphic(stackPane);


            	}
        	}
    	});


    	selectColumn.setPrefWidth(22);


    	return selectColumn;
	}


	public TableColumn<Rental, String> getInvoiceColumn(){
    	invoiceColumn.setCellValueFactory(new PropertyValueFactory<>("deliveryTime"));


    	invoiceColumn.setCellFactory(column -> new TableCell<Rental, String>() {
        	private final String invoiceImagePath = new String("/images/create-invoices.png");
        	private final Image invoiceImage = getCachedImage(invoiceImagePath);
        	private final ImageView invoiceImageView = new ImageView(invoiceImage);


        	{
            	invoiceImageView.setFitHeight(27);
            	invoiceImageView.setPreserveRatio(true);
        	}


        	@Override
        	protected void updateItem(String item, boolean empty) {
            	super.updateItem(item, empty);




            	if (empty || item == null) {
                	setText(null);
                	setGraphic(null);
                	setMinHeight(Config.DB_ROW_HEIGHT_EMPTY);
                	setMaxHeight(Config.DB_ROW_HEIGHT_EMPTY);
                	setPrefHeight(Config.DB_ROW_HEIGHT_EMPTY);
            	} else {
                	setMinHeight(Config.DB_ROW_HEIGHT);
                	setMaxHeight(Config.DB_ROW_HEIGHT);
                	setPrefHeight(Config.DB_ROW_HEIGHT);




                	Rental rental = getTableView().getItems().get(getIndex());
                	VBox vBox = new VBox();
                	vBox.setSpacing(-6);
                	vBox.setAlignment(Pos.BOTTOM_CENTER);




                	// Top section handling (optional "Send to QuickBooks" image)
                	if (rental.isWritingInvoice()) {
                    	ImageView sendImage = createImageView("/images/send-to-quickbooks.png", 34);
                    	Tooltip tooltip = new Tooltip("Ready to Send");
                    	tooltip.setShowDelay(Duration.ZERO);
                    	Tooltip.install(sendImage, tooltip);
                    	vBox.getChildren().add(createHBox(sendImage));
                	}




                	// Bottom section handling (invoice status and check/X icon)
                	HBox bottomBox = createBottomPane(rental.isInvoiceWritten(), rental.getNeedsInvoice());
                	bottomBox.setStyle("-fx-padding: 0 0 13 0;");
                	vBox.getChildren().add(bottomBox);
               	 
                	vBox.setStyle("-fx-padding: 0 -5 0 -5");
                	setGraphic(vBox);
            	}
        	}




        	// Helper method for creating the top HBox with an image
        	private HBox createHBox(Node node) {
            	HBox hBox = new HBox();
            	hBox.getChildren().add(node);
            	hBox.setAlignment(Pos.CENTER);
            	return hBox;
        	}




        	// Helper method for creating the bottom pane with the image and status
        	private HBox createBottomPane(boolean isInvoiceWritten, boolean needsInvoice) {
            	HBox hBox = new HBox();
            	hBox.setSpacing(0);
            	hBox.setAlignment(Pos.CENTER_LEFT);


            	Label statusLabel;
            	if (!needsInvoice) {
                	Label checkSymbol = new Label("\u2713");
                	checkSymbol.setStyle("-fx-text-fill: orange; -fx-font-size: 18px; -fx-padding: 0;");
                	statusLabel = new Label("  Not\n  due");
                	hBox.getChildren().addAll(invoiceImageView, checkSymbol, statusLabel);
            	} else {
                	if (isInvoiceWritten) {
                    	Label checkSymbol = new Label("\u2713"); // Unicode checkmark symbol
                    	checkSymbol.setStyle("-fx-text-fill: green; -fx-font-size: 18px; -fx-padding: 0;");
                    	statusLabel = new Label(" Has\n invoice");
                    	hBox.getChildren().addAll(invoiceImageView, checkSymbol, statusLabel);
                	} else {
                    	Label xSymbol = new Label("\u2717"); // Unicode X symbol
                    	xSymbol.setStyle("-fx-text-fill: red; -fx-font-size: 18px; -fx-padding: -2;");
                    	statusLabel = new Label("  Needs\n  invoice");
                    	hBox.getChildren().addAll(invoiceImageView, xSymbol, statusLabel);
                	}
            	}


            	statusLabel.setStyle("-fx-font-size: 9; -fx-padding: 0 -2 0 -2;");
            	return hBox;
        	}




        	// Helper method to create an ImageView from an image path
        	private ImageView createImageView(String path, int fitHeight) {
            	Image image = getCachedImage(path);
            	ImageView imageView = new ImageView(image);
            	imageView.setFitHeight(fitHeight);
            	imageView.setPreserveRatio(true);
            	return imageView;
        	}
    	});




    	invoiceColumn.setPrefWidth(72); // Adjust width as needed
    	return invoiceColumn;
	}




	public TableColumn<Rental, String> getBillColumn(){
    	billColumn.setCellValueFactory(new PropertyValueFactory<>("latestBillDate"));
    	billColumn.setCellFactory(column -> new TableCell<Rental, String>() {
        	private final String billImagePath = new String("/images/bill.png");
        	private final Image billImage = getCachedImage(billImagePath);
        	private final ImageView billImageView = new ImageView(billImage);


        	{
            	billImageView.setFitHeight(18);
            	billImageView.setPreserveRatio(true);
        	}


        	@Override
        	protected void updateItem(String item, boolean empty) {
            	super.updateItem(item, empty);
       	 
            	if (empty || item == null) {
                	setText(null);
                	setGraphic(null);
                	setMinHeight(Config.DB_ROW_HEIGHT_EMPTY);
                	setMaxHeight(Config.DB_ROW_HEIGHT_EMPTY);
                	setPrefHeight(Config.DB_ROW_HEIGHT_EMPTY);
            	} else {
                	setMinHeight(Config.DB_ROW_HEIGHT);
                	setMaxHeight(Config.DB_ROW_HEIGHT);
                	setPrefHeight(Config.DB_ROW_HEIGHT);


                	Rental rental = getTableView().getItems().get(getIndex());
                	VBox vBox = new VBox(billImageView);
                	vBox.setAlignment(Pos.BOTTOM_CENTER);
               	 


                	setGraphic(vBox);
            	}
       	 
       	 
        	}
       	 
    	});
    	billColumn.setPrefWidth(72);
    	return billColumn;
	}






	public TableColumn<Rental, String> getAddressColumn(){


    	return customerAndAddressColumn;


	}




	public TableColumn<Rental, Boolean> getStatusColumn(){




    	return statusColumn;
	}


	public void setLiftTypeLabel(Label liftTypeLabel){
    	globalLiftTypeLabel = liftTypeLabel;
	}


	public Label getLiftTypeLabel(){
    	return globalLiftTypeLabel;
	}


	private void handleSelection(boolean isSelected, int index, String actionType) {
    	dbTableView.getItems().get(index).setSelected(isSelected);


    	// Show the update button only if at least one row is selected
    	boolean anySelected = dbTableView.getItems().stream().anyMatch(Rental::isSelected);
    	updateRentalButton.setVisible(anySelected);


    	if (actionType.equals("composing-contracts")) {
        	batchButton.setText("Batch Contracts");
            batchButton.setVisible(anySelected);
            updateRentalButton.setVisible(false);

			
            if (anySelected) {
				List<String> createdPdfFiles = new ArrayList<>();
                handleBatchContracts(createdPdfFiles);
            }
        }


    	if (actionType.equals("composing-invoices")) {
        	batchButton.setText("Batch Invoices");
        	batchButton.setVisible(anySelected);
        	updateRentalButton.setVisible(false);
        	if (anySelected) {
            	batchButton.setText("Batch Invoices");
            	batchButton.setVisible(true);
            	updateRentalButton.setVisible(false);
            	batchButton.setOnAction(event -> {
                	System.out.println("Batch button clicked."); // Debugging entry point
               	 
                	// Filter selected rentals
                	ObservableList<Rental> selectedRentals = dbTableView.getItems().filtered(Rental::isSelected);
                	System.out.println("Selected rentals: " + selectedRentals.size());
           	 
           	 
                	if (selectedRentals.isEmpty()) {
                    	System.out.println("No rentals selected. Cannot compose invoices.");
                    	return;
                	}
           	 
           	 
                	System.out.println("Clearing all composing invoices in the database...");
                	clearAllComposingInvoiceInDB();
           	 
           	 
                	// Loop through each selected rental and flag it in the database
                	boolean anyUpdated = false;
                	for (Rental rental : selectedRentals) {
                    	System.out.println("Flagging rental item ID: " + rental.getRentalItemId());
                    	boolean updateSuccess = flagComposingInvoiceInDB(rental.getRentalItemId());
           	 
           	 
                    	if (updateSuccess) {
                        	anyUpdated = true;
                        	rental.setWritingInvoice(true);
                        	System.out.println("Successfully flagged rental item ID: " + rental.getRentalItemId());
                    	} else {
                        	System.out.println("Failed to update rental item ID: " + rental.getRentalItemId());
                    	}
                	}
           	 
           	 
                	// Show confirmation if any updates were successful
                	if (anyUpdated) {
                    	System.out.println("At least one rental item flagged successfully.");
           	 
           	 
                    	// Path to the Python script
                        String scriptPath = Paths.get(PathConfig.INVOICES_DIR, ".venv", "Scripts", "make_invoices_from_queue.py").toString();
                    	System.out.println("Python script path: " + scriptPath);
           	 
           	 
                    	// Execute the Python script
                    	System.out.println("Attempting to execute the Python script...");
                    	boolean scriptExecutionSuccess = executePythonScript();
           	 
           	 
                    	if (scriptExecutionSuccess) {
                        	System.out.println("Python script executed successfully.");
                    	} else {
                        	System.out.println("Python script execution failed.");
                    	}
           	 
           	 
                    	// Prepare secondary button for invoices
                    	prepareSecondaryButtonForInvoices();
                    	secondInProcessButton.setVisible(true);
                	} else {
                    	System.out.println("No rental items were updated.");
                	}
           	 
           	 
                	System.out.println("Refreshing the table view...");
                	dbTableView.refresh();
            	});
           	 




            	ObservableList<Rental> selectedRentals = dbTableView.getItems().filtered(Rental::isSelected);
            	secondInProcessButton.setOnAction(event -> {


                	if (invoiceSecondarySwitcher == "open-sdk") {
                    	runSDKTool();
                    	// copy request file string
                    	prepareSecondaryButtonForConfirmation();

                        String filePath = Paths.get(PathConfig.INVOICES_DIR, "invoice_batch.xml").toString();
                    	StringSelection stringSelection = new StringSelection(filePath);
                    	Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);
                	} else if (invoiceSecondarySwitcher == "confirmation") {
                    	System.out.println("secondary button pressed and about to update invoice vars");
                    	updateInvoiceVarsInSQL(selectedRentals);
                    	System.out.println("Made it past the invoice vars sql call");
                    	resetCheckboxes();
                    	batchButton.setVisible(false);
                    	secondInProcessButton.setVisible(false);
                    	showSelectableCheckboxes(false, lastActionType);
                    	dbTableView.refresh();
                    	parent.shiftSidebarHighlighter(null);
                    	lastActionType = null;


                	}
            	});
        	}


    	}
	}




	private void handleExpandSelection(int index) {
    	MaxReachPro.setRentalForExpanding(dbTableView.getItems().get(index), "Activity");
    	imageCache.clear();
    	try {
        	MaxReachPro.loadScene("/fxml/expand.fxml");
        	GradientAnimator.initialize();
    	} catch (Exception e) {
        	e.printStackTrace();
    	}
	}


	public void showSelectableCheckboxes(boolean visible, String actionType) {
    	lastActionType = actionType;
    	boolean shouldShow = "dropping-off".equals(actionType) ||
            	"calling-off".equals(actionType) ||
            	"picking-up".equals(actionType) ||
            	"composing-invoices".equals(actionType) ||
            	"composing-contracts".equals(actionType) ||
            	"deleting".equals(actionType);


    	this.shouldShowCheckboxes = shouldShow && visible; // Set the class-level variable
    	dbTableView.refresh();
	}


	public void showExpandIcons(boolean visible) {
    	if (visible) {
        	lastActionType = "expanding";
    	} else {
        	lastActionType = null;
    	}
    	dbTableView.refresh();
	}


	public void resetCheckboxes() {
    	// Deselect all checkboxes in the table
    	for (Rental order : dbTableView.getItems()) {
        	order.setSelected(false);
    	}
    	dbTableView.refresh(); // Refresh the table view to update the checkbox states
	}


	private Set<String> calculatePotentialDrivers(Rental rental) {
		Set<String> potentialDrivers = new HashSet<>();
	
		// Add the "x" option for clearing the driver.
		potentialDrivers.add("x");
	
		// Get the current driver assigned to this rental.
		String currentDriver = rental.getDriver();
		String[] extractedDriverVars = extractDriverInitialsAndNumber(currentDriver);
		String currentDriverInitial = extractedDriverVars[0];
		String currentDriverNumber = extractedDriverVars[1];
	
		// Initialize a map to count the sequences of drivers by their initials.
		Map<String, Integer> driverSequenceMap = new HashMap<>();
	
		// Populate the sequence map with the existing drivers
		for (String driver : existingDrivers) {
			if (driver != null && !driver.equals("x")) {
				String initial = driver.replaceAll("[^A-Za-z]", "");
				int newSequence = driverSequenceMap.getOrDefault(initial, 0) + 1;
				driverSequenceMap.put(initial, newSequence);
			}
		}
	
		// Add potential drivers based on the initials and count
		for (String driverInitial : driverInitials) {
			int count = driverSequenceMap.getOrDefault(driverInitial, 0);
	
			// Adding the current driver without increment
			if (driverInitial.equals(currentDriverInitial)) {
				for (int i = 1; i <= count; i++) {
					String potentialDriver = driverInitial + i; // Append the count
					potentialDrivers.add(potentialDriver);
				}
			} else {
				// Adding initial with a sequence number
				for (int i = 1; i <= count + 1; i++) {
					String potentialDriver = driverInitial + i; // Append the count
					potentialDrivers.add(potentialDriver);
				}
	
				// If there are no existing rentals for this initial, we still want to add the first option
				if (count == 0) {
					potentialDrivers.add(driverInitial + "1");
				}
			}
		}
	
		return potentialDrivers;
	}
	

	public void loadExistingDrivers() {
		String query = "SELECT driver FROM rental_items WHERE driver IS NOT NULL AND driver != 'x';";
		try (Connection connection = DriverManager.getConnection(Config.DB_URL, Config.DB_USR, Config.DB_PSWD);
			 PreparedStatement preparedStatement = connection.prepareStatement(query);
			 ResultSet resultSet = preparedStatement.executeQuery()) {
	
			while (resultSet.next()) {
				String driver = resultSet.getString("driver");
				existingDrivers.add(driver);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("Loaded existing driver assignments and got: " + existingDrivers);
	}
	



	private void updateGroupedRentals(String driverValue, Rental rental) {
    	// Extract letters (initials) and numbers (sequence) from the new driver value
    	System.out.println("New Driver Value: " + driverValue);


    	String newInitial = driverValue.replaceAll("[^A-Za-z]", "");  // Extract letters only
    	String newStringSeqNumber = driverValue.replaceAll("[^0-9]", "");  // Extract digits only


    	System.out.println("New Initial: " + newInitial);
    	System.out.println("New Sequence Number: " + newStringSeqNumber);


    	// Get the current driver assigned to this rental
    	String currentDriver = rental.getDriver();
    	System.out.println("Old Driver Value: " + currentDriver);


    	// Extract initials and sequence from the old driver value
    	String oldInitial = currentDriver.replaceAll("[^A-Za-z]", "");  // Extract letters only
    	String oldSeqNumber = currentDriver.replaceAll("[^0-9]", "");  // Extract digits only


    	System.out.println("Old Initial: " + oldInitial);
    	System.out.println("Old Sequence Number: " + oldSeqNumber);


    	// Convert sequence numbers to integers for comparisons, if needed


    	int newIntSeqNum = newStringSeqNumber.isEmpty() ? 0 : parseInt(newStringSeqNumber);
    	int oldIntSeqNum = oldSeqNumber.isEmpty() ? 0 : parseInt(oldSeqNumber);


    	// Adjust sequence for rentals with the new initial
    	List<Rental> newRentals = groupedRentals.get(newInitial);


    	if (!driverValue.equals("x")) {
        	if (newRentals != null) {
            	System.out.println("Derived newRentals for the new initial '" + newInitial + "': " + newRentals);
            	System.out.println("Processing rentals with new initial: " + newInitial);
            	System.out.println("Current driver sequence: " + driverSequenceMap.get(newInitial));


            	// Increment sequence numbers for rentals with the new initial that are higher
            	for (Rental r : newRentals) {
                	System.out.println("Checking rental: " + r);


                	String sequenceStringOfRentalBeingChecked = String.valueOf(r.getSequenceNumber());
                	int sequenceIntOfRentalBeingChecked = r.getSequenceNumber();
                	System.out.println("Sequence number of rental being checked: " + sequenceStringOfRentalBeingChecked);


                	if (sequenceIntOfRentalBeingChecked >= newIntSeqNum) {
                    	System.out.println("Updating rental from new initial: " + r);
                    	int newSequenceInt = sequenceIntOfRentalBeingChecked + 1;
                    	String newSequenceString = String.valueOf(newSequenceInt);
                    	r.setDriver(newInitial + newSequenceString); // Update driver value
                    	System.out.println("Set driver to: " + r.getDriver());
                	} else {
                    	System.out.println("Skipping rental (below the sequence of the new rental value): " + r);
                	}
            	}
        	} else {
            	System.out.println("No rentals found with initial: " + newInitial);
        	}
    	}


    	// Adjust sequence for rentals with the old initial
    	List<Rental> currentRentals = groupedRentals.get(oldInitial);
    	if (currentRentals != null) {
        	System.out.println("Derived currentRentals for the old initial '" + oldInitial + "': " + currentRentals);
        	System.out.println("Processing rentals with old initial: " + oldInitial);
        	System.out.println("Current driver sequence: " + driverSequenceMap.get(oldInitial));


        	// Decrement sequence numbers for rentals with the old initial that are higher
        	for (Rental r : currentRentals) {
            	System.out.println("Checking rental: " + r);


            	String sequenceStringOfRentalBeingChecked = String.valueOf(r.getSequenceNumber());
            	int sequenceIntOfRentalBeingChecked = r.getSequenceNumber();
            	System.out.println("Sequence number of rental being checked: " + sequenceStringOfRentalBeingChecked);


            	if (sequenceIntOfRentalBeingChecked > oldIntSeqNum) {
                	System.out.println("Updating rental from old initial: " + r);
                	int newSequenceInt = sequenceIntOfRentalBeingChecked - 1;
                	String newSequenceString = String.valueOf(newSequenceInt);
                	r.setDriver(oldInitial + newSequenceString); // Update driver value
                	System.out.println("Set driver to: " + r.getDriver());
            	}
        	}
        	// Remove the rental from the old initial list
        	currentRentals.remove(rental);
        	System.out.println("Removed rental: " + rental + " from old initial: " + oldInitial);


        	// If there are no more rentals for the old initial, remove it from the map
        	if (currentRentals.isEmpty()) {
            	groupedRentals.remove(oldInitial);
            	System.out.println("Removed old initial: " + oldInitial + " from groupedRentals.");
        	}
    	}


    	// Now, set the new driver for the rental
    	rental.setDriver(driverValue); // Set the new driver value
    	System.out.println("Set new driver for rental: " + rental + " to: " + driverValue);


    	// Update the sequence number of the rental
    	if (newIntSeqNum != 0) {
        	rental.setSequenceNumber(newIntSeqNum); // Set the new sequence number
        	System.out.println("Set sequence number for rental: " + rental + " to: " + newIntSeqNum);
    	}


    	// Add the rental to the new driver's list
    	groupedRentals.computeIfAbsent(newInitial, k -> new ArrayList<>()).add(rental);
    	System.out.println("Added rental: " + rental + " to new initial: " + newInitial);
	}




	private void updateDriverInDatabase(int rentalItemId, String newDriver) {
		// Extract initials and number from the newDriver argument
		String[] driverParts = extractDriverInitialsAndNumber(newDriver);
	
		// Update the driver, driver_initial, and driver_number columns
		String updateQuery = "UPDATE rental_items SET driver = ?, driver_initial = ?, driver_number = ? WHERE rental_item_id = ?";
	
		try (Connection connection = DriverManager.getConnection(Config.DB_URL, Config.DB_USR, Config.DB_PSWD);
			 PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {
	
			// Set parameters for the prepared statement
			preparedStatement.setString(1, newDriver);
			preparedStatement.setString(2, driverParts[0]);
			preparedStatement.setInt(3, Integer.parseInt(driverParts[1])); // Convert the number part to int
			preparedStatement.setInt(4, rentalItemId);
	
			// Execute the update
			preparedStatement.executeUpdate();
	
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	


	private String[] extractDriverInitialsAndNumber(String driverName) {
		// Use a regular expression to separate the letters and numbers
		String initials = driverName.replaceAll("\\d", ""); // Remove all digits to get initials
		String number = driverName.replaceAll("\\D", ""); // Remove all non-digits to get the number
		
		return new String[] { initials.toUpperCase(), number };
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
    	if (phoneNumber == null || phoneNumber.length() != 10) {
        	throw new IllegalArgumentException("Input must be a 10-digit number.");
    	}


    	return "(" + phoneNumber.substring(0, 3) + ")-" + phoneNumber.substring(3, 6) + "-" + phoneNumber.substring(6, 10);
	}


	private void updateInvoiceVarsInSQL(List<Rental> selectedRentals) {
    	System.out.println("Entered updateInvoiceVarsInSQL method");
    
    
    	// Check if the list is null or empty
    	if (selectedRentals == null || selectedRentals.isEmpty()) {
        	System.out.println("No rentals provided, exiting method.");
        	return;
    	}
    
    
    	// Extract rental item IDs from selected rentals, filtering out nulls
    	List<Integer> rentalItemIds = selectedRentals.stream()
            	.filter(Objects::nonNull) // Filter out null Rental objects
            	.map(Rental::getRentalItemId) // Map to rental item IDs
            	.filter(Objects::nonNull) // Filter out null IDs
            	.collect(Collectors.toList());
    
    
    	// Log extracted IDs
    	System.out.println("Rental Item IDs: " + rentalItemIds);
    
    
    	// Check if the list of IDs is empty after processing
    	if (rentalItemIds.isEmpty()) {
        	System.out.println("No valid rental IDs found, exiting method.");
        	return;
    	}
    
    
    	// SQL query for batch processing
    	String updateQuery = """
        	UPDATE rental_items
        	SET
            	invoice_composed = CASE
                	WHEN rental_item_id IN (%s) THEN 1
                	ELSE invoice_composed
            	END,
            	last_billed_date = CURRENT_DATE,
            	composing_invoice = 0
        	WHERE rental_item_id IN (%s);
    	""";
    
    
    	// Convert rentalItemIds to a comma-separated string
    	String idList = rentalItemIds.stream()
            	.map(String::valueOf)
            	.collect(Collectors.joining(","));
    	String formattedQuery = String.format(updateQuery, idList, idList);
    
    
    	// Log final SQL query
    	System.out.println("Final SQL Query: " + formattedQuery);
    
    
    	// Variable to track success
    	boolean success = false;
    
    
    	// Execute the SQL update
    	try (Connection connection = DriverManager.getConnection(Config.DB_URL, Config.DB_USR, Config.DB_PSWD);
         	PreparedStatement statement = connection.prepareStatement(formattedQuery)) {
    
    
        	int rowsUpdated = statement.executeUpdate();
        	System.out.println(rowsUpdated + " rows updated successfully.");
    
    
        	success = rowsUpdated > 0;
    
    
    	} catch (SQLException e) {
        	System.err.println("SQL exception while updating invoice variables: " + e.getMessage());
        	e.printStackTrace();
    	}
    
    
    	// If the update was successful, update the local objects
    	if (success) {
        	String todayDate = LocalDate.now().toString();
    
    
        	for (Rental rental : selectedRentals) {
            	if (rental != null && rentalItemIds.contains(rental.getRentalItemId())) {
                	rental.setInvoiceWritten(true);
                	rental.setWritingInvoice(false);
                	System.out.println("Updated rental ID: " + rental.getRentalItemId());
            	}
    
    
            	if ("Active".equals(rental.getStatus()) && rental.isWritingInvoice()) {
                	rental.setLatestBilledDate(todayDate);
                	System.out.println("Updated most recent bill for rental ID: " + rental.getRentalItemId());
            	}
        	}
    	} else {
        	System.out.println("No rows were updated in the database.");
    	}
	}
    


	private void clearAllComposingInvoiceInDB() {
    	String updateQuery = "UPDATE rental_items SET composing_invoice = 0 WHERE composing_invoice = 1";
    	boolean success = false;


    	try (Connection connection = DriverManager.getConnection(Config.DB_URL, Config.DB_USR, Config.DB_PSWD);
         	PreparedStatement statement = connection.prepareStatement(updateQuery)) {


        	int rowsUpdated = statement.executeUpdate();


        	if (rowsUpdated > 0) {
            	success = true;
        	} else {
        	}


    	} catch (SQLException e) {
        	System.err.println("SQL exception while updating composing invoices: " + e.getMessage());
        	e.printStackTrace();
    	}


    	for (Rental rental : dbTableView.getItems()) {
        	rental.setWritingInvoice(false);
    	}
	}


	private boolean flagComposingInvoiceInDB(int rentalItemId) {
    	String updateQuery = "UPDATE rental_items SET composing_invoice = 1 WHERE rental_item_id = ?";
    	boolean success = false;




    	try (Connection connection = DriverManager.getConnection(Config.DB_URL, Config.DB_USR, Config.DB_PSWD);
         	PreparedStatement statement = connection.prepareStatement(updateQuery)) {




        	statement.setInt(1, rentalItemId);
        	int rowsUpdated = statement.executeUpdate();




        	if (rowsUpdated > 0) {
            	success = true;
        	} else {
        	}




    	} catch (SQLException e) {
        	System.err.println("SQL exception while updating rental item ID " + rentalItemId + ": " + e.getMessage());
        	e.printStackTrace();
    	}




    	return success;
	}


	private boolean executePythonScript() {
    	try {
        	System.out.println("Preparing to execute Python script...");
    
    
        	// Set up the Python interpreter path (modify if needed)
            String pythonPath = Paths.get(System.getProperty("user.home"), 
            "OneDrive", "Documents", "MaxReachPro", "SMM Filing", "venv", "Scripts", "python.exe").toString();
            String scriptPath = Paths.get(PathConfig.BASE_DIR, "Composing Invoices", ".venv", "Scripts", "make_invoices_from_queue.py").toString();
    
    
        	// Create the process builder with Python and the script path
        	ProcessBuilder processBuilder = new ProcessBuilder(pythonPath, scriptPath);
    
    
        	// Set working directory to ensure relative paths in the script work
            File workingDirectory = new File(PathConfig.INVOICES_DIR);
        	if (workingDirectory.exists() && workingDirectory.isDirectory()) {
            	processBuilder.directory(workingDirectory);
            	System.out.println("Working directory set to: " + workingDirectory.getAbsolutePath());
        	} else {
            	System.err.println("Invalid working directory: " + workingDirectory.getAbsolutePath());
            	return false;
        	}
    
    
        	// Log the full command for debugging
        	System.out.println("Executing command: " + String.join(" ", processBuilder.command()));
    
    
        	// Redirect error stream to merge with standard output
        	processBuilder.redirectErrorStream(true);
    
    
        	// Start the Python process
        	Process process = processBuilder.start();
    
    
        	// Capture and print output from the Python script
        	System.out.println("Python script output:");
        	try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            	String line;
            	while ((line = reader.readLine()) != null) {
                	System.out.println("[Python]: " + line);
            	}
        	}
    
    
        	// Wait for the process to finish and capture the exit code
        	int exitCode = process.waitFor();
        	System.out.println("Python script exited with code: " + exitCode);
    
    
        	// Check exit code for success or failure
        	if (exitCode != 0) {
            	System.err.println("Python script returned an error. Exit code: " + exitCode);
            	return false;
        	}
    
    
    	} catch (IOException e) {
        	System.err.println("IOException while executing Python script: " + e.getMessage());
        	e.printStackTrace();
        	return false;
    	} catch (InterruptedException e) {
        	System.err.println("Script execution was interrupted: " + e.getMessage());
        	e.printStackTrace();
        	return false;
    	} catch (Exception e) {
        	System.err.println("Unexpected error: " + e.getMessage());
        	e.printStackTrace();
        	return false;
    	}
    
    
    	System.out.println("Python script executed successfully.");
    	return true;
	}
   
	private void runSDKTool() {
    	// Debug output path


    	File sdkToolFile = new File(Config.SDK_PATH);
    	if (!sdkToolFile.exists()) {
        	return;
    	}




    	new Thread(() -> {
        	try {
            	ProcessBuilder processBuilder = new ProcessBuilder(Config.SDK_PATH);
            	processBuilder.redirectErrorStream(true);
            	Process process = processBuilder.start();


        	} catch (IOException e) {
            	e.printStackTrace();
        	}
    	}).start();
	}


	private void prepareSecondaryButtonForInvoices(){
    	invoiceSecondarySwitcher = "open-sdk";
    	secondInProcessButton.setGraphic(null);
    	secondInProcessButton.setText(null);
    	String imagePath = "/images/send-to-quickbooks.png";
    	ImageView imageView = createImageView(imagePath, 17);
    	imageView.setFitWidth(20);


    	HBox hbox = new HBox();
    	hbox.getChildren().addAll(new Label("Open SDK  "), imageView);
    	hbox.setAlignment(Pos.CENTER);


    	secondInProcessButton.setGraphic(hbox);
	}


	private void prepareSecondaryButtonForConfirmation() {
    	invoiceSecondarySwitcher = "confirmation";
    	secondInProcessButton.setGraphic(null);
    	secondInProcessButton.setText(null);
    	String imagePath = "/images/send-to-quickbooks.png";
    	ImageView imageView = createImageView(imagePath, 17);
    	imageView.setFitWidth(20);


    	HBox hbox = new HBox();
    	hbox.getChildren().addAll(new Label("Mark as Written  "), imageView);
    	hbox.setAlignment(Pos.CENTER);


    	secondInProcessButton.setGraphic(hbox);
	}


	private void updateInvoiceWrittenInDB(int rentalItemId) {
    	String updateQuery = "UPDATE rental_items SET invoice_composed = 1 WHERE rental_item_id = ?";
    	boolean success = false;


    	try (Connection connection = DriverManager.getConnection(Config.DB_URL, Config.DB_USR, Config.DB_PSWD);
         	PreparedStatement statement = connection.prepareStatement(updateQuery)) {


        	statement.setInt(1, rentalItemId);
        	int rowsUpdated = statement.executeUpdate();


        	if (rowsUpdated > 0) {
            	success = true;
        	} else {
            	System.out.println("No rows updated for rental item ID: " + rentalItemId);
        	}


    	} catch (SQLException e) {
        	System.err.println("SQL exception while updating rental item ID " + rentalItemId + ": " + e.getMessage());
        	e.printStackTrace();
    	}
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

	private void handleBatchContracts(List<String> createdPdfFiles) {
		ObservableList<Rental> selectedRentals = dbTableView.getItems().filtered(Rental::isSelected);

		if (selectedRentals.isEmpty()) {
			return;
		}

		String sourceFile = Paths.get(PathConfig.CONTRACTS_DIR, "contract template.pdf").toString();

		for (Rental rental : selectedRentals) {
			String outputFile = Paths.get(PathConfig.CONTRACTS_DIR, "contract_" + rental.getRentalItemId() + ".pdf").toString();
			double latitude = rental.getLatitude();
			double longitude = rental.getLongitude();
			String mapPage = getGridNameFromCoords(latitude, longitude);

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
				LocalDate date = LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE);
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM'.' d");
				String formattedDate = date.format(formatter);
				int day = date.getDayOfMonth();
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
				canvas.setTextMatrix(43, 523);
				canvas.showText(rental.getLocationNotes());
				canvas.setTextMatrix(81, 630); // Name
				canvas.showText(rental.getName());
				canvas.setTextMatrix(43, 652);
				canvas.showText(rental.getPreTripInstructions());
				canvas.setTextMatrix(99, 481); // Lift Type
				canvas.showText(rental.getLiftType());

				canvas.endText();

				// Close the document
				document.close();

				// Track the generated PDF file
				createdPdfFiles.add(outputFile);

			secondInProcessButton.setVisible(true);
			} catch (Exception e) {
				System.out.println("Error creating contract for rental ID " + rental.getRentalItemId() + ": " + e.getMessage());
				e.printStackTrace();
			}
		}
	}


	private Image getCachedImage(String path) {
    	return imageCache.computeIfAbsent(path, p -> new Image(getClass().getResourceAsStream(p)));
	}


	private ImageView createImageView(String path, int fitHeight) {
    	Image image = getCachedImage(path);
    	ImageView imageView = new ImageView(image);
    	imageView.setFitHeight(fitHeight);
    	imageView.setPreserveRatio(true);
    	return imageView;
	}


}


