package sample;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Controller {
    // VARIABLE INITIALIZATIONS
    private List<Todo> todos = new ArrayList<Todo>();
    private List<Todo> doneTodos = new ArrayList<Todo>();
    private List<Todo> undoneTodos = new ArrayList<Todo>();
    @FXML
    public TextField addTodoTextField;
    @FXML
    public ListView<BorderPane> todoListView;
    @FXML
    public ToggleGroup filters; // The group of toggles pertaining to filters




    // -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=INITIALIZATION SECTION-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-


    // WE ARE USING THIS CODE TO READ THE TODOS FROM THEIR FILE AND REPOPULATE THE ARRAYLIST
    @FXML
    private void  initialize() {

        // REPOPULATE ARRAYLIST OF TO-DOS
        try (ObjectInputStream todoFile = new ObjectInputStream(new BufferedInputStream(new FileInputStream("src/sample/Assets/Data/todoFile.dat")))) {
            boolean eof = false; // eof = End of File
            while (!eof) {
                try {
                    // Get the to-do from the file
                    Todo todo = (Todo) todoFile.readObject();
                    System.out.println("Todo found: " + todo.getTodo());
                    // Add the to-do into the list
                    todos.add(todo);
                } catch (EOFException e) {
                    eof = true; // will exit while loop
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        for(Object key : filters.getProperties().keySet()){
            System.out.println("Key is " + key);
        }
        // Add all elements in the todos list to the listview
        populateTodos(todos);

        // CREATE FILTERED VERSIONS OF THE ORIGINAL TODOLIST
        for(Todo todo : todos){
            if(todo.isDone()){
                doneTodos.add(todo);
            } else{
                undoneTodos.add(todo);
            }
        }
        // BIND THESE LISTS TO CORRESPONDING TOGGLES
        for(Toggle toggle : filters.getToggles()){
            // We must cast this to a radio btn bcs the getText() method is not available on the parent Toggle class
            RadioButton radioBtn = (RadioButton) toggle;
            String radioName = radioBtn.getText();
            // Depending on the toggle, assign corresponding filtered lists to their toggles as user data
            if(radioName.equalsIgnoreCase("All")){
                toggle.setUserData(todos);
            } else if(radioName.equalsIgnoreCase("Done")){
                toggle.setUserData(doneTodos);
            } else {
                toggle.setUserData(undoneTodos);
            }
        }

        // ADD A CHANGE LISTENER TO AUTOMATE FILTERING
        filters.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> observableValue, Toggle oldToggle, Toggle newToggle) {
                if(filters.getSelectedToggle() != null) { // Checking to see if a radio is even selected
                    // Since getUserData() returns an object, we have to cast it to the type of list we want
                    populateTodos((List<Todo>)filters.getSelectedToggle().getUserData());
                }
            }
        });

    }



    // -=-=-=-=-=-=-=-=-=-=-=-=-=-=-ADDING, REMOVING, MAKING, SAVING, FINISHING TODOS-=-=-=-=-=-==-=-=-=-=-=-=-=-=-=-=-=-


    // THE CODE TO ADD A TO DO AND SET AND EVENT LISTENER ON ALL OF THEM
    public void addTodo() {

        // Create and add the to do to the list
        String todoText = addTodoTextField.getText(); // Retrieve text from textFiel
        Todo newTodo = new Todo(todoText); // Use the text to create a new to-do
        todos.add(newTodo); // Add the to-do to the todos list
        undoneTodos.add(newTodo); // By default, the to-do will be undone
        addTodoTextField.setText(""); // Reset the textField
        System.out.println("You have added a todo: " + newTodo.getTodo()); // Log the addition

        makeTodo(newTodo);


    }


    // TO DELETE A TO-DO
    private void removeTodo(Todo todo, BorderPane todoHolder) {
        // Remove to-do from all lists (filtered and unfiltered)
        todos.remove(todo); // removes from main list
        if(todo.isDone()) { // Removes from one of the filtered list
            doneTodos.remove(todo);
        } else {
            undoneTodos.remove(todo);
        }

        todoListView.getItems().remove(todoHolder); // removes from UI
        System.out.println("You have removed a todo: " + todo.getTodo());
    }


    // TO PUT A TO-DO IN AN HBOX AND ADD ITS EVENT LISTENERS
    private void makeTodo(Todo newTodo) {
        // ALL OF THIS CODE IS SIMPLY FOR MAKING A TO-DO (The specifics aren't terribly important
        Label todoLabel = new Label(newTodo.getTodo());
        todoLabel.setAlignment(Pos.BASELINE_LEFT);

        Button deleteTodo = new Button();
        deleteTodo.getStyleClass().add("deleteTodo"); // Add a styleclass

        Button doneTodo = new Button();
        doneTodo.getStyleClass().add("doneTodo");
        finishTodo(todoLabel, doneTodo, !newTodo.isDone()); // In case the to-do has been read from a file and is already finished

        // Put the buttons in an HBox
        HBox buttonHolder = new HBox(); // This HBox will be used to store the buttons, so they can be added to the holder
        buttonHolder.getChildren().addAll(doneTodo, deleteTodo); // Add the buttons in

        // Then, put it all in the a BorderPane
        BorderPane todoHolder = new BorderPane();
        todoHolder.setLeft(todoLabel); // The actual to-do (the text) will be on the left side of holder
        todoHolder.setRight(buttonHolder); // Add both buttons at once to the right of the holder

        // Add the BorderPane to the ListView
        todoListView.getItems().add(todoHolder);

        // ACTION LISTENERS
        deleteTodo.setOnAction(new EventHandler<>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                removeTodo(newTodo, todoHolder);
            }
        });
        doneTodo.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                if(newTodo.isDone()) { // If the to-do is done
                    finishTodo(todoLabel, doneTodo, true); // Switch from done to undone
                    doneTodos.remove(newTodo); // Remove from the list of done to-dos
                    undoneTodos.add(newTodo); // Add to the list of undone to-dos
                } else { // If the to-do isn't done
                    finishTodo(todoLabel, doneTodo, false); // Switch from undone to done
                    doneTodos.add(newTodo); // Add to the list of done to-dos
                    undoneTodos.remove(newTodo); // Remove from the list of undone to-dos
                }
                newTodo.setDone(!(newTodo.isDone())); // reverse the object's done property to undone or vice versa
            }
        });

    }


    // THIS CODE SHOULD WRITE THE TODOS TO A FILE
    public void saveTodos() {
        try(ObjectOutputStream todoFile = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream("src/sample/Assets/Data/todoFile.dat")))){
            for (Todo todo : todos) {
                todoFile.writeObject(todo);
                System.out.println("Saved todo: " + todo.getTodo());
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }


    // THIS IS FOR REPOPULATING TODOS
    public void populateTodos(List<Todo> todoList) { // This argument will be used to filter to-dos
        // We must removeAll because we want to repopulate the listView every time we filter todos
        todoListView.getItems().clear();
        todoListView.refresh(); // The listview doesn't automatically refresh unless you .bind() a list to it
        for (Todo todo : todoList) {
            makeTodo(todo); // Add the to-do to the UI
        }
    }


    // TO MARK A TO-DO AS DONE OR UNDONE AGAIN (put a through the text)
    public void finishTodo(Label todo, Button isDoneButton, boolean isDone) {

        if(!isDone) {
            // Strike the text through
            todo.getStyleClass().add("strikethrough"); // Adds the strikethrough class
            isDoneButton.getStyleClass().clear();
            isDoneButton.getStyleClass().add("undoneTodo");
        } else {
            // Reverse the strike through
            todo.getStyleClass().remove("strikethrough"); // Removes the strikethrough class
            isDoneButton.getStyleClass().clear();
            isDoneButton.getStyleClass().add("doneTodo");
        }

    }





}
