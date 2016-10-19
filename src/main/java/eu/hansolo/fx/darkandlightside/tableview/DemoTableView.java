package eu.hansolo.fx.darkandlightside.tableview;

import javafx.application.Application;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.scene.layout.StackPane;
import javafx.scene.Scene;
import javafx.util.Callback;
import javafx.util.converter.DefaultStringConverter;

import java.math.BigDecimal;
import java.time.LocalDate;


public class DemoTableView extends Application {
    private TableViewFX tableViewFX;
    private Point2D     currentCell;
    private Point2D     startCell;
    private Point2D     endCell;

    @Override public void init() {
        tableViewFX = createTableView();
    }

    @Override public void start(Stage stage) {
        StackPane pane = new StackPane(tableViewFX);
        pane.setPadding(new Insets(20));

        Scene scene = new Scene(pane);

        stage.setTitle("TableView Demo");
        stage.setScene(scene);
        stage.show();
    }

    @Override public void stop() {
        System.exit(0);
    }

    private TableViewFX createTableView() {
        Callback<TableColumn<Person, String>, TableCell<Person, String>> editingCellFactory = (TableColumn<Person, String> p) -> new EditableTableCell<>(new DefaultStringConverter());

        SelectableTableColumn<Person, String> firstNameColumn = new SelectableTableColumn<>("First Name");
        firstNameColumn.setCellValueFactory(p -> p.getValue().firstNameProperty());
        firstNameColumn.setCellFactory(p -> new SelectableTableCell<>());

        SelectableTableColumn<Person, String> lastNameColumn = new SelectableTableColumn<>("Last Name");
        lastNameColumn.setCellValueFactory(p -> p.getValue().lastNameProperty());
        lastNameColumn.setCellFactory(p -> new SelectableTableCell<>());
        //lastNameColumn.setOnEditCommit(event -> event.getTableView().getItems().get(event.getTablePosition().getRow()).setInfo(event.getNewValue().toString()));

        SelectableTableColumn<Person, String> infoColumn = new SelectableTableColumn<>("Info");
        infoColumn.setCellValueFactory(p -> p.getValue().infoProperty());
        infoColumn.setCellFactory(editingCellFactory);
        //infoColumn.setOnEditCommit(event -> event.getTableView().getItems().get(event.getTablePosition().getRow()).setLastName(event.getNewValue().toString()));

        SelectableTableColumn<Person, LocalDate> dateColumn = new SelectableTableColumn<>("Date");
        dateColumn.setCellValueFactory(p -> p.getValue().birthdayProperty());
        dateColumn.setCellFactory(p -> new EditableDateTableCell<>());

        SelectableTableColumn<Person, BigDecimal> incomeColumn = new SelectableTableColumn<>("Income");
        incomeColumn.setCellValueFactory(p -> p.getValue().moneyProperty());
        incomeColumn.setCellFactory(p -> new EditableCurrencyTableCell<>());

        SelectableTableColumn<Person, BigDecimal> numberColumn = new SelectableTableColumn<>("Number");
        numberColumn.setCellValueFactory(p -> p.getValue().numberProperty());
        numberColumn.setCellFactory(p -> new EditableNumberTableCell<>());

        TableViewFX<Person> tableView = new TableViewFX<>(FXCollections.observableArrayList(
            new Person("Han", "Solo", "Captain of the Millennium Falcon"),
            new Person("Luke", "Skywalker", "Jedi Knight"),
            new Person("Lando", "Calrissian", "Baron Administrator"),
            new Person("Leia", "Skywalker", "General Alliance"),
            new Person("Anakin", "Skywalker", "Jedi Knight"),
            new Person("Padmé", "Amidala", "Politician"),
            new Person("Obi-Wan", "Kenobi", "Jedi Master"),
            new Person("Count", "Dooku", "Sith Lord"),
            new Person("Kylo", "Ren", "Jedi Trainee")));
        tableView.getColumns().addAll(firstNameColumn, lastNameColumn, infoColumn, dateColumn, incomeColumn, numberColumn);
        tableView.setEditable(true);

        tableView.getFocusModel().focusedCellProperty().addListener(o -> {
            System.out.println("Focused Cell: " + tableView.getFocusModel().getFocusedCell().getColumn() + ", " + tableView.getFocusModel().getFocusedCell().getRow());
            currentCell = new Point2D(tableView.getFocusModel().getFocusedCell().getColumn(), tableView.getFocusModel().getFocusedCell().getRow());
        });
        tableView.setOnMouseDragEntered(e -> {
            startCell = currentCell;
        });
        tableView.setOnMouseDragReleased(e -> {
            endCell = currentCell;
            System.out.println(startCell);
            System.out.println(endCell);
        });

        tableView.setPrefSize(600, 400);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        return tableView;
    }

    public static void main(String[] args) {
        launch(args);
    }


    // ******************** Inner Classes *************************************
    public class Person {
        private StringProperty             firstName;
        private StringProperty             lastName;
        private StringProperty             street;
        private IntegerProperty            postalCode;
        private StringProperty             city;
        private ObjectProperty<LocalDate>  birthday;
        private StringProperty             info;
        private ObjectProperty<BigDecimal> money;
        private ObjectProperty<BigDecimal> number;


        public Person() {
            this(null, null, null);
        }
        public Person(final String FIRST_NAME, final String LAST_NAME, final String INFO) {
            firstName  = new SimpleStringProperty(FIRST_NAME);
            lastName   = new SimpleStringProperty(LAST_NAME);
            street     = new SimpleStringProperty("some street");
            postalCode = new SimpleIntegerProperty(1234);
            city       = new SimpleStringProperty("some city");
            birthday   = new SimpleObjectProperty<>(LocalDate.of(1999, 2, 21));
            info       = new SimpleStringProperty(INFO);
            money      = new SimpleObjectProperty<>(BigDecimal.ZERO);
            number     = new SimpleObjectProperty<>(BigDecimal.ZERO);
        }

        public String getFirstName() { return firstName.get(); }
        public void setFirstName(final String FIRST_NAME) { firstName.set(FIRST_NAME); }
        public StringProperty firstNameProperty() { return firstName; }

        public String getLastName() { return lastName.get(); }
        public void setLastName(final String LAST_NAME) { lastName.set(LAST_NAME); }
        public StringProperty lastNameProperty() { return lastName; }

        public String getStreet() { return street.get(); }
        public void setStreet(final String STREET) { street.set(STREET); }
        public StringProperty streetProperty() { return street; }

        public int getPostalCode() { return postalCode.get(); }
        public void setPostalCode(final int POSTAL_CODE) { postalCode.set(POSTAL_CODE); }
        public IntegerProperty postalCodeProperty() { return postalCode; }

        public String getCity() { return city.get(); }
        public void setCity(final String CITY) { city.set(CITY); }
        public StringProperty cityProperty() { return city; }

        public LocalDate getBirthday() { return birthday.get(); }
        public void setBirthday(final LocalDate BIRTHDAY) { birthday.set(BIRTHDAY); }
        public ObjectProperty<LocalDate> birthdayProperty() { return birthday; }

        public String getInfo() { return info.get(); }
        public void setInfo(final String INFO) { info.set(INFO); }
        public StringProperty infoProperty() { return info; }

        public BigDecimal getMoney() { return money.get(); }
        public void setMoney(final BigDecimal INCOME) { money.set(INCOME); }
        public ObjectProperty<BigDecimal> moneyProperty() { return money; }

        public BigDecimal getNumber() { return number.get(); }
        public void setNumber(final BigDecimal VALUE) { number.set(VALUE); }
        public ObjectProperty<BigDecimal> numberProperty() { return number; }
    }
}
