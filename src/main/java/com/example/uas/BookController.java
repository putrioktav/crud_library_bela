package com.example.uas;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BookController {
    private static final Logger LOGGER = Logger.getLogger(BookController.class.getName());

    @FXML
    private TableView<Book> tableView;
    @FXML
    private TableColumn<Book, Integer> idColumn;
    @FXML
    private TableColumn<Book, String> titleColumn;
    @FXML
    private TableColumn<Book, String> authorColumn;
    @FXML
    private TableColumn<Book, String> publisherColumn;
    @FXML
    private TableColumn<Book, Integer> yearColumn;
    @FXML
    private TableColumn<Book, String> statusColumn;

    @FXML
    private TextField idField;
    @FXML
    private TextField titleField;
    @FXML
    private TextField authorField;
    @FXML
    private TextField publisherField;
    @FXML
    private TextField yearField;
    @FXML
    private ComboBox<String> statusComboBox;

    private ObservableList<Book> bookList = FXCollections.observableArrayList();

    public void initialize() {
        // Mengatur kolom TableView
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        authorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));
        publisherColumn.setCellValueFactory(new PropertyValueFactory<>("publisher"));
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("year"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        tableView.setItems(bookList);

        // Mengisi ComboBox dengan status
        statusComboBox.getItems().addAll("Available", "Checked Out");

        // Memuat data buku dari database
        loadBooks();
    }

    private void loadBooks() {
        try (Connection connection = Database.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM books")) {
            bookList.clear();
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String title = resultSet.getString("title");
                String author = resultSet.getString("author");
                String publisher = resultSet.getString("publisher");
                int year = resultSet.getInt("year");
                String status = resultSet.getString("status");
                bookList.add(new Book(id, title, author, publisher, year, status));
            }
            tableView.setItems(bookList);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading books", e);
        }
    }

    @FXML
    public void addBook() {
        System.out.println("Tombol Tambah ditekan");
        try {
            int id = generateId();
            String title = titleField.getText();
            String author = authorField.getText();
            String publisher = publisherField.getText();
            String yearText = yearField.getText();
            String status = statusComboBox.getValue();

            // Validasi input
            if (title.isEmpty() || author.isEmpty() || publisher.isEmpty() || yearText.isEmpty() || status == null) {
                System.out.println("Semua field harus diisi!");
                return;
            }

            int year = Integer.parseInt(yearText);

            Book newBook = new Book(id, title, author, publisher, year, status);
            bookList.add(newBook);
            tableView.setItems(bookList);

            System.out.println("Buku berhasil ditambahkan ke list");

            // Simpan buku baru ke database
            String query = "INSERT INTO books (id, title, author, publisher, year, status) VALUES (?, ?, ?, ?, ?, ?)";
            try (Connection connection = Database.getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, id);
                preparedStatement.setString(2, title);
                preparedStatement.setString(3, author);
                preparedStatement.setString(4, publisher);
                preparedStatement.setInt(5, year);
                preparedStatement.setString(6, status);
                preparedStatement.executeUpdate();
                System.out.println("Buku berhasil disimpan ke database");
            }
        } catch (NumberFormatException e) {
            System.out.println("Tahun harus berupa angka!");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error adding book", e);
        }
    }

    @FXML
    public void editBook() {
        Book selectedBook = tableView.getSelectionModel().getSelectedItem();
        if (selectedBook == null) {
            System.out.println("Pilih buku yang ingin diedit!");
            return;
        }

        idField.setText(String.valueOf(selectedBook.getId()));
        titleField.setText(selectedBook.getTitle());
        authorField.setText(selectedBook.getAuthor());
        publisherField.setText(selectedBook.getPublisher());
        yearField.setText(String.valueOf(selectedBook.getYear()));
        statusComboBox.setValue(selectedBook.getStatus());

        System.out.println("Form siap untuk diedit");
    }

    @FXML
    public void saveBook() {
        Book selectedBook = tableView.getSelectionModel().getSelectedItem();
        if (selectedBook == null) {
            System.out.println("Pilih buku yang ingin disimpan!");
            return;
        }

        try {
            int id = Integer.parseInt(idField.getText());
            String title = titleField.getText();
            String author = authorField.getText();
            String publisher = publisherField.getText();
            int year = Integer.parseInt(yearField.getText());
            String status = statusComboBox.getValue();

            // Validasi input
            if (title.isEmpty() || author.isEmpty() || publisher.isEmpty() || yearField.getText().isEmpty() || status == null) {
                System.out.println("Semua field harus diisi!");
                return;
            }

            selectedBook.setTitle(title);
            selectedBook.setAuthor(author);
            selectedBook.setPublisher(publisher);
            selectedBook.setYear(year);
            selectedBook.setStatus(status);
            tableView.refresh();

            // Update database
            String query = "UPDATE books SET title = ?, author = ?, publisher = ?, year = ?, status = ? WHERE id = ?";
            try (Connection connection = Database.getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, title);
                preparedStatement.setString(2, author);
                preparedStatement.setString(3, publisher);
                preparedStatement.setInt(4, year);
                preparedStatement.setString(5, status);
                preparedStatement.setInt(6, id);
                preparedStatement.executeUpdate();
                System.out.println("Buku berhasil disimpan ke database");
            }
        } catch (NumberFormatException e) {
            System.out.println("Tahun harus berupa angka!");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error saving book", e);
        }
    }

    @FXML
    public void deleteBook() {
        Book selectedBook = tableView.getSelectionModel().getSelectedItem();
        if (selectedBook == null) {
            System.out.println("Pilih buku yang ingin dihapus!");
            return;
        }

        bookList.remove(selectedBook);

        // Hapus dari database
        String query = "DELETE FROM books WHERE id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, selectedBook.getId());
            preparedStatement.executeUpdate();
            System.out.println("Buku berhasil dihapus dari database");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting book", e);
        }
    }

    private int generateId() {
        try (Connection connection = Database.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT MAX(id) AS max_id FROM books")) {
            if (resultSet.next()) {
                return resultSet.getInt("max_id") + 1;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error generating ID", e);
        }
        return 1; // Default jika tabel kosong
    }
}
