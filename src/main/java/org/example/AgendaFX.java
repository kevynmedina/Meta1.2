package org.example;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class AgendaFX extends Application {

    private PersonaService personaService = new PersonaService(new PersonaDAOMariaDB());

    private ListView<String> listaPersonas = new ListView<>();
    private Map<String, Integer> personaIdMap = new HashMap<>();

    private TextField nombreField = new TextField();
    private ListView<Direccion> direccionesListView = new ListView<>();
    private TextField telefonoField = new TextField();
    private ListView<String> telefonosListView = new ListView<>();

    private ComboBox<String> tipoDireccionCombo = new ComboBox<>();
    private TextField direccionField = new TextField();

    private Button agregarDireccionBtn = new Button("Agregar Dirección");
    private Button eliminarDireccionBtn = new Button("Eliminar Dirección");
    private Button agregarTelefonoBtn = new Button("Agregar Teléfono");
    private Button eliminarTelefonoBtn = new Button("Eliminar Teléfono");

    private Button agregarBtn = new Button("Agregar Persona");
    private Button actualizarBtn = new Button("Modificar Persona");
    private Button eliminarBtn = new Button("Eliminar Persona");
    private Button limpiarBtn = new Button("Limpiar Campos");

    @Override
    public void start(Stage primaryStage) {
        VBox root = new VBox(10);
        root.setPadding(new Insets(15));

        try {
            List<String> tipos = personaService.obtenerTiposDireccion();
            tipoDireccionCombo.getItems().clear();
            tipoDireccionCombo.getItems().addAll(tipos);
            if (!tipoDireccionCombo.getItems().isEmpty()) {
                tipoDireccionCombo.getSelectionModel().selectFirst();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            tipoDireccionCombo.getItems().clear();
            tipoDireccionCombo.getItems().addAll("Casa", "Oficina", "Departamento", "Sucursal");
            tipoDireccionCombo.getSelectionModel().selectFirst();
            mostrarAlerta("Error", "No se pudieron cargar los tipos de dirección desde la base de datos.");
        }


        nombreField.setPromptText("Nombre");
        direccionField.setPromptText("Dirección");
        telefonoField.setPromptText("Teléfono");
        direccionesListView.setCellFactory(param -> new ListCell<Direccion>() {
            @Override
            protected void updateItem(Direccion item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toString());
                }
            }
        });

        VBox panelDirecciones = new VBox(5);
        panelDirecciones.getChildren().addAll(
                new Label("Direcciones:"),
                new HBox(5, tipoDireccionCombo, direccionField, agregarDireccionBtn, eliminarDireccionBtn),
                direccionesListView
        );

        VBox panelTelefonos = new VBox(5);
        panelTelefonos.getChildren().addAll(
                new Label("Teléfonos:"),
                new HBox(5, telefonoField, agregarTelefonoBtn, eliminarTelefonoBtn),
                telefonosListView
        );

        HBox botonesPrincipales = new HBox(5);
        botonesPrincipales.getChildren().addAll(agregarBtn, actualizarBtn, eliminarBtn, limpiarBtn);
        configurarAcciones();
        actualizarLista();
        root.getChildren().addAll(
                new Label("Nombre:"),
                nombreField,
                panelDirecciones,
                panelTelefonos,
                botonesPrincipales,
                new Label("Personas:"),
                listaPersonas
        );

        Scene scene = new Scene(root, 600, 700);
        primaryStage.setTitle("Agenda");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void configurarAcciones() {
        agregarDireccionBtn.setOnAction(e -> {
            String tipo = tipoDireccionCombo.getValue();
            String direccion = direccionField.getText().trim();

            if (tipo != null && !direccion.isEmpty()) {
                direccionesListView.getItems().add(new Direccion(0, tipo, direccion));
                direccionField.clear();
            } else {
                mostrarAlerta("Error", "Seleccione un tipo y escriba una dirección.");
            }
        });

        eliminarDireccionBtn.setOnAction(e -> {
            Direccion seleccionada = direccionesListView.getSelectionModel().getSelectedItem();
            if (seleccionada != null) {
                direccionesListView.getItems().remove(seleccionada);
            }
        });

        agregarTelefonoBtn.setOnAction(e -> {
            String telefono = telefonoField.getText().trim();
            if (!telefono.isEmpty()) {
                telefonosListView.getItems().add(telefono);
                telefonoField.clear();
            }
        });

        eliminarTelefonoBtn.setOnAction(e -> {
            String seleccionado = telefonosListView.getSelectionModel().getSelectedItem();
            if (seleccionado != null) {
                telefonosListView.getItems().remove(seleccionado);
            }
        });

        agregarBtn.setOnAction(e -> {
            try {
                String nombre = nombreField.getText().trim();
                List<Direccion> direcciones = new ArrayList<>(direccionesListView.getItems());
                List<String> telefonos = new ArrayList<>(telefonosListView.getItems());

                if (nombre.isEmpty()) {
                    mostrarAlerta("Error", "El nombre es obligatorio.");
                    return;
                }

                personaService.agregarPersona(nombre, direcciones, telefonos);
                actualizarLista();
                limpiarCampos();
            } catch (SQLException ex) {
                ex.printStackTrace();
                mostrarAlerta("Error", "No se pudo agregar la persona: " + ex.getMessage());
            }
        });

        actualizarBtn.setOnAction(e -> {
            try {
                String selected = listaPersonas.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    int id = personaIdMap.get(selected.split(" - ")[0]);
                    String nombre = nombreField.getText().trim();
                    List<Direccion> direcciones = new ArrayList<>(direccionesListView.getItems());
                    List<String> telefonos = new ArrayList<>(telefonosListView.getItems());

                    if (nombre.isEmpty()) {
                        mostrarAlerta("Error", "El nombre es obligatorio.");
                        return;
                    }

                    personaService.modificarPersona(id, nombre, direcciones, telefonos);
                    actualizarLista();
                } else {
                    mostrarAlerta("Error", "Seleccione una persona para modificar.");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                mostrarAlerta("Error", "No se pudo actualizar la persona: " + ex.getMessage());
            }
        });


        eliminarBtn.setOnAction(e -> {
            try {
                String selected = listaPersonas.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    int id = personaIdMap.get(selected.split(" - ")[0]);

                    Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
                    confirmacion.setTitle("Confirmar eliminación");
                    confirmacion.setHeaderText("¿Está seguro de eliminar esta persona?");

                    Optional<ButtonType> resultado = confirmacion.showAndWait();
                    if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
                        personaService.eliminarPersona(id);
                        actualizarLista();
                        limpiarCampos();
                    }
                } else {
                    mostrarAlerta("Error", "Seleccione una persona para eliminar.");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                mostrarAlerta("Error", "No se pudo eliminar la persona: " + ex.getMessage());
            }
        });


        limpiarBtn.setOnAction(e -> limpiarCampos());

        listaPersonas.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                try {
                    int id = personaIdMap.get(newVal.split(" - ")[0]);
                    Persona persona = personaService.obtenerPersonaPorId(id);

                    if (persona != null) {
                        nombreField.setText(persona.getNombre());
                        direccionesListView.getItems().setAll(persona.getDirecciones());
                        telefonosListView.getItems().setAll(persona.getTelefonos());
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    mostrarAlerta("Error", "No se pudo cargar la información de la persona.");
                }
            }
        });

    }

    private void actualizarLista() {
        try {
            List<Persona> personas = personaService.obtenerPersonas();
            listaPersonas.getItems().clear();
            personaIdMap.clear();

            for (Persona p : personas) {
                String telefonos = p.getTelefonos().isEmpty() ? "Sin teléfonos" :
                        String.join(", ", p.getTelefonos());

                String direcciones = p.getDirecciones().isEmpty() ? "Sin direcciones" :
                        p.getDirecciones().stream()
                                .map(Direccion::toString)
                                .collect(Collectors.joining("; "));

                String info = p.getNombre()+" - "+telefonos+" - "+direcciones;
                listaPersonas.getItems().add(info);
                personaIdMap.put(p.getNombre(), p.getId());
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            mostrarAlerta("Error", "No se cargaron las personas: "+ex.getMessage());
        }
    }


    private void limpiarCampos() {
        nombreField.clear();
        direccionesListView.getItems().clear();
        telefonosListView.getItems().clear();
        direccionField.clear();
        telefonoField.clear();
        listaPersonas.getSelectionModel().clearSelection();

        if (!tipoDireccionCombo.getItems().isEmpty()) {
            tipoDireccionCombo.getSelectionModel().selectFirst();
        }
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
