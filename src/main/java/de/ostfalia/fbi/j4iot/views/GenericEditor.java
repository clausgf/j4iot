package de.ostfalia.fbi.j4iot.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import de.f0rce.ace.AceEditor;
import de.f0rce.ace.enums.AceMode;
import de.f0rce.ace.enums.AceTheme;
import de.ostfalia.fbi.j4iot.data.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.List;

@CssImport(
        themeFor = "vaadin-grid",
        value = "./themes/j4iot/views/generic-editor.css"
)
public abstract class GenericEditor  extends Div implements BeforeEnterObserver {
    private Logger log = LoggerFactory.getLogger(GenericEditor.class);

    private static final String ZONE_ID = "Europe/Berlin";
    private static final String BROWSER_ROOT = "/iot-data/";
    protected final TreeGrid<File> tree = new TreeGrid<>();
    private File opendFile = null;
    protected final String COLUMN_FILE_KEY = "file_column";
    protected final String COLUMN_EDIT_KEY = "edit_column";

    private final AceEditor editor = new AceEditor();
    private final Button editorSaveButton = new Button("Save");
    private final Button editorCancelButton = new Button("Cancel");
    private final Button editorOpenButton = new Button("Open");
    private final Button editorDeleteButton = new Button("Delete");
    private final Button editorRenameButton = new Button("Rename");

    private TextField name = new TextField("Filename");
    private DateTimePicker createdAt = new DateTimePicker("Created at");
    private DateTimePicker updatedAt = new DateTimePicker("Updated at");
    private TextField size = new TextField("Size");

    protected FileService fileService;


    public GenericEditor(FileService fileService) {
        this.fileService = fileService;

        editor.setTheme(AceTheme.chrome);
        editor.setMode(AceMode.java);
        editor.setValue("");
        editor.setHeight("100%");
        addClassNames("file-editor-view");

        // Create UI
        HorizontalLayout layout = new HorizontalLayout();
        createBrowserLayout(layout);
        createEditorLayout(layout);
        createMetadataLayout(layout);
        add(layout);
    }

    private File getSelectedFile(){
        Set<File> selected = tree.getSelectedItems();
        return selected.isEmpty() ? null : selected.iterator().next();
    }

    private File getOpendFile(){
        return opendFile;
    }

    private void closeOpenedFile(){
        opendFile = null;
    }

    private boolean isFileOpened(){
        return opendFile != null;
    }

    private void openSelectedFile() throws IOException {
        File file = getSelectedFile();
        if (file != null && file.isFile()){
            openFile(file);
        }
    }

    private void openFile(File file) throws IOException {
        if (file == null){
            return;
        }
        if (file.isFile()){
            editor.setValue(Files.readString(file.toPath()));
            AceMode mode = AceMode.json;
            if (file.getName().endsWith("json")){
                mode = AceMode.json;
            }else if(file.getName().endsWith("sql")){
                mode = AceMode.sql;
            }
            editor.setMode(mode);
            editor.focus();
        }else{
            tree.expand(file);
        }
        opendFile = file;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        TreeData<File> data = getData();
        tree.setDataProvider(new TreeDataProvider<>(data));
        tree.recalculateColumnWidths();
        opendFile = null;
    }

    protected abstract TreeData<File> getData();

    private void setMetaData(File file) throws IOException {
        if (file != null){
            BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            name.setValue(file.getName());
            createdAt.setValue(LocalDateTime.ofInstant(attr.creationTime().toInstant(), ZoneId.of(ZONE_ID)));
            updatedAt.setValue(LocalDateTime.ofInstant(attr.lastModifiedTime().toInstant(), ZoneId.of(ZONE_ID)));
            size.setValue(sizeConverter(attr.size()));
        }else{
            name.setValue("");
            createdAt.setValue(null);
            updatedAt.setValue(null);
            size.setValue("");
        }
    }

    private String sizeConverter(long size){
        String[] units = {"Byte", "KB", "MB", "GB", "TB"};
        double curSize = size;
        int i = 0;
        while(curSize > 1024){
            if (curSize > 1024){
                curSize /= 1024;
            }
            i++;
        }
        return curSize + " " + units[i];
    }

    private void createBrowserLayout(HorizontalLayout layout){
        Div wrapper = new Div();
        wrapper.setClassName("grid-wrapper");
        wrapper.setWidth("30%");

        tree.setSelectionMode(Grid.SelectionMode.SINGLE);
        tree.addSelectionListener(x -> {
            try {
                if (x.getFirstSelectedItem().isPresent()){
                    setMetaData(x.getFirstSelectedItem().get());
                    if (x.getFirstSelectedItem().get().isFile()){
                        openSelectedFile();
                    }
                }else{
                    setMetaData(null);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        tree.addHierarchyColumn(x -> x.getName()).setFlexGrow(1).setKey(COLUMN_FILE_KEY)
                .setComparator((s3fileA, s3fileB) -> {
                    if(!s3fileA.isFile() && !s3fileB.isFile()) {
                        return s3fileA.getName().compareToIgnoreCase(s3fileB.getName());
                    }else if(!s3fileA.isFile() && s3fileB.isFile()) {
                        return 1;
                    }else if(s3fileA.isFile() && s3fileB.isFile()) {
                        return s3fileA.getName().compareToIgnoreCase(s3fileB.getName());
                    }else {
                        return -1;
                    }
                });
        tree.addComponentColumn(x -> {
            Button button = null;
            if (!x.isFile()){
                button = new Button();
                button.setIcon(VaadinIcon.PLUS.create());
                button.addClickListener(e -> fileBrowserAdd_Click(x));
            }
            return button;
        }).setFlexGrow(0).setKey(COLUMN_EDIT_KEY);
        tree.getColumns().forEach(x -> x.setAutoWidth(true));
        tree.sort(GridSortOrder.desc(tree.getColumnByKey(COLUMN_FILE_KEY)).build());
        wrapper.add(tree);
        layout.add(wrapper);
    }

    private void fileBrowserAdd_Click(File file) {
        try{
            //TODO
            for(int i = 0; i < 100; i++){
                String newName = "new_file_" + i;
                Path newPath = Paths.get(file.getPath(), newName);
                if (!Files.exists(newPath)){
                    File newFile = new File(String.valueOf(newPath));
                    newFile.createNewFile();
                    addFileToBrowser(newFile, file);
                    tree.expand(file);
                    tree.select(newFile);
                    openFile(newFile);
                    break;
                }
            }
        }
        catch(IOException ex){
            throw new RuntimeException(ex);
        }
    }

    private void createMetadataLayout(HorizontalLayout layout) {
        Div editorLayout = new Div();
        editorLayout.setClassName("editor-layout");
        editorLayout.setWidth("20%");

        H3 header = new H3("Metadata");
        header.setClassName("editor-header");
        editorLayout.add(header);

        Div editorDiv = new Div();
        editorDiv.setClassName("editor-main");
        editorLayout.add(editorDiv);

        FormLayout formLayout = new FormLayout();
        //name.setClassName("editor-overwrite");
        name.setId("my-text-field");
        createdAt.setReadOnly(true);
        updatedAt.setReadOnly(true);
        size.setReadOnly(true);
        formLayout.add(name, createdAt, updatedAt, size);

        editorDiv.add(formLayout);

        layout.add(editorLayout);
    }

    private void createEditorLayout(HorizontalLayout layout) {
        Div wrapper = new Div();
        wrapper.setClassName("grid-wrapper");
        wrapper.setWidth("50%");
        layout.add(wrapper);
        wrapper.add(editor);
        createEditorButtonLayout(wrapper);
    }

    private void createEditorButtonLayout(Div layout){
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setClassName("editor-footer");

        editorRenameButton.addClickListener(e -> editorRename_Click());
        editorRenameButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        editorSaveButton.addClickListener(e -> editorSave_Click());
        editorSaveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        editorDeleteButton.addClickListener(e -> editorDelete_Click());
        editorDeleteButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        editorCancelButton.addClickListener(e -> editorCancel_Click());
        editorCancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        buttonLayout.add(editorRenameButton, editorSaveButton, editorDeleteButton, editorCancelButton);
        layout.add(buttonLayout);

    }

    private void editorRename_Click(){
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("File name already used!");
        dialog.setText("A file with the same file name already exists. Would you like to overwrite the file?");
        dialog.setCancelable(true);
        dialog.addCancelListener(e -> {
            try {
                setMetaData(getOpendFile());
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        dialog.setConfirmText("Confirm");
        dialog.addConfirmListener(e -> renameOpendFile(name.getValue()));
        if (isFileOpened()){
            File file = getOpendFile();
            if (!file.getName().equals(name.getValue())){
                if (Files.exists(Paths.get(file.getParent(), name.getValue()))){
                    dialog.open();
                }else{
                    renameOpendFile(name.getValue());
                }
            }
        }else{
            getOpenFileDialog().open();
        }
    }

    private void renameOpendFile(String newName){
        File file = getOpendFile();
        File newFile = new File(String.valueOf(Paths.get(file.getParent(), newName)));
        tree.deselect(file);
        file.renameTo(newFile);
        if (!tree.getTreeData().contains(newFile)){
            addFileToBrowser(newFile, tree.getTreeData().getParent(file));
        }
        deleteFileInBrowser(file);
        String editorValue = editor.getValue();
        tree.select(newFile);
        editor.setValue(editorValue);
    }

    private void editorSave_Click(){
        try{
            if (isFileOpened()){
                File file = getOpendFile();
                BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                writer.write(editor.getValue());
                writer.close();
            }else{
                getOpenFileDialog().open();
            }
        }
        catch(IOException ex){
            throw new RuntimeException(ex);
        }
    }

    private void editorDelete_Click(){
        if (isFileOpened()){
            File file = getOpendFile();
            file.delete();
            deleteFileInBrowser(file);
        }else{
            getOpenFileDialog().open();
        }
    }

    private void editorCancel_Click(){
        editor.setValue("");
        closeOpenedFile();
        tree.deselect(getSelectedFile());
    }

    private void addFileToBrowser(File file, File parent){
        tree.getTreeData().addItem(parent, file);
        tree.getDataProvider().refreshAll();
    }

    private void deleteFileInBrowser(File file){
        tree.getTreeData().removeItem(file);
        tree.getDataProvider().refreshAll();
    }

    private ConfirmDialog getOpenFileDialog(){
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("No file opened!");
        dialog.setText("Please open a file first.");
        dialog.setConfirmText("Confirm");
        return dialog;
    }
}