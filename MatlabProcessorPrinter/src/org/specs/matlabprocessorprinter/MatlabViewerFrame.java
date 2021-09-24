/**
 * Copyright 2015 SPeCS.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License. under the License.
 */

package org.specs.matlabprocessorprinter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.specs.MatlabIR.MatlabLanguage.LanguageMode;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.FileNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.FunctionNode;
import org.specs.MatlabProcessor.MatlabParser.MatlabParser;
import org.specs.MatlabToC.jOptions.MatlabToCOptionUtils;
import org.specs.matisselib.io.PostTypeInferenceRecipeReader;
import org.specs.matisselib.io.PreTypeInferenceSsaRecipeReader;
import org.specs.matisselib.ssa.SsaPass;
import org.specs.matisselib.typeinference.PostTypeInferencePass;
import org.specs.matlabprocessorprinter.printers.CodePrinter;
import org.specs.matlabprocessorprinter.syntaxkits.AstSyntaxKit;
import org.specs.matlabprocessorprinter.syntaxkits.BytecodeSyntaxKit;
import org.specs.matlabprocessorprinter.syntaxkits.LaraSyntaxKit;
import org.specs.matlabprocessorprinter.syntaxkits.MatlabSyntaxKit;
import org.specs.matlabtocl.v2.CLRecipes;
import org.suikasoft.jOptions.Interfaces.DataStore;

import jsyntaxpane.DefaultSyntaxKit;
import pt.up.fe.specs.matisse.weaver.MWeaverRun;
import pt.up.fe.specs.matisse.weaver.MWeaverUtils;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsLogs;

public class MatlabViewerFrame extends JFrame {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private final JTabbedPane codeTab;
    private final JEditorPane matlabCodeView;
    private final JScrollPane matlabCodeScroll;
    private final JEditorPane laraCodeView;
    private final JScrollPane laraCodeScroll;
    private final JTabbedPane outputTab;
    private final JTextPane diagnosticsView;
    private final JScrollPane diagnosticsScroll;
    private final List<JRadioButtonMenuItem> modeItems = new ArrayList<>();
    private PrintMode currentMode;
    private LanguageMode languageMode = LanguageMode.MATLAB;

    private final JCheckBoxMenuItem applyPassesItem;
    private final JCheckBoxMenuItem convertToCssaItem;
    private final JCheckBoxMenuItem enableZ3Item;
    private final JCheckBoxMenuItem useSharedMemoryItem;
    private final JCheckBoxMenuItem subgroupAsWarpFallbackItem;
    private final JCheckBoxMenuItem dumpSsaItem;
    private final JCheckBoxMenuItem dumpOutputTypesItem;

    private final Style commonStyle, errorStyle;
    private final List<String> passesToLog = new ArrayList<>();

    private final CodeGenerationSettings codeGenSettings = new CodeGenerationSettings() {

        @Override
        public LanguageMode getLanguageMode() {
            return languageMode;
        }

        @Override
        public boolean applyPasses() {
            return applyPassesItem.isSelected();
        }

        @Override
        public boolean convertToCssa() {
            return convertToCssaItem.isSelected();
        }

        @Override
        public boolean enableZ3() {
            return enableZ3Item.isSelected();
        }

        @Override
        public boolean dumpSsa() {
            return dumpSsaItem.isSelected();
        }

        @Override
        public boolean dumpOutputTypes() {
            return dumpOutputTypesItem.isSelected();
        }

        @Override
        public boolean useUnifiedMemory() {
            return useSharedMemoryItem.isSelected();
        }

        @Override
        public boolean enableOpenCLProfiling() {
            return true;
        }

        public List<String> getPassesToLog() {
            return passesToLog;
        }

        public boolean useSubgroupAsWarpFallback() {
            return subgroupAsWarpFallbackItem.isSelected();
        }
    };

    public MatlabViewerFrame() {
        super("MATISSE AST");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        DefaultSyntaxKit.initKit();
        DefaultSyntaxKit.registerContentType("text/matlab", MatlabSyntaxKit.class.getName());
        DefaultSyntaxKit.registerContentType("text/lara", LaraSyntaxKit.class.getName());
        DefaultSyntaxKit.registerContentType("application/x-matisse-bytecode", BytecodeSyntaxKit.class.getName());
        DefaultSyntaxKit.registerContentType("application/x-matisse-ast", AstSyntaxKit.class.getName());

        this.matlabCodeView = new JEditorPane();
        this.matlabCodeScroll = new JScrollPane(this.matlabCodeView);
        this.matlabCodeView.setContentType("text/matlab");
        this.laraCodeView = new JEditorPane();
        this.laraCodeScroll = new JScrollPane(this.laraCodeView);
        this.laraCodeView.setContentType("text/lara");

        this.codeTab = new JTabbedPane();
        this.codeTab.addTab("MATLAB", this.matlabCodeScroll);
        this.codeTab.addTab("LARA", this.laraCodeScroll);

        this.outputTab = new JTabbedPane();

        this.diagnosticsView = new JTextPane();
        this.diagnosticsScroll = new JScrollPane(this.diagnosticsView);

        this.commonStyle = this.diagnosticsView.addStyle("common", null);
        StyleConstants.setForeground(this.commonStyle, Color.BLACK);
        this.errorStyle = this.diagnosticsView.addStyle("error", null);
        StyleConstants.setForeground(this.errorStyle, Color.RED);

        this.diagnosticsView.setEditable(false);
        this.diagnosticsView.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem newItem = new JMenuItem("New");
        newItem.addActionListener(evt -> {
            this.matlabCodeView.setText("");
            this.outputTab.removeAll();
        });
        fileMenu.add(newItem);
        JMenuItem openItem = new JMenuItem("Open File...");
        openItem.addActionListener(evt -> {
            JFileChooser chooser = new JFileChooser();
            int returnVal = chooser.showOpenDialog(MatlabViewerFrame.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                try {
                    try (FileReader reader = new FileReader(chooser.getSelectedFile())) {
                        this.matlabCodeView.setText(readAll(reader));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        fileMenu.add(openItem);
        menuBar.add(fileMenu);

        JMenu languageMenu = new JMenu("Language");
        ButtonGroup languageButtonGroup = new ButtonGroup();

        for (LanguageMode languageMode : LanguageMode.values()) {
            final LanguageMode language = languageMode;
            JRadioButtonMenuItem languageItem = new JRadioButtonMenuItem(languageMode.getLanguageName());
            languageButtonGroup.add(languageItem);
            languageMenu.add(languageItem);

            if (languageMode == LanguageMode.MATLAB) {
                languageItem.setSelected(true);
            }

            languageItem.addActionListener(evt -> {
                this.languageMode = language;
                parseCode();
            });
        }

        menuBar.add(languageMenu);

        JMenu parseMenu = new JMenu("MATISSE");

        ButtonGroup parseOutputGroup = new ButtonGroup();

        for (PrintMode mode : PrintMode.values()) {
            JRadioButtonMenuItem item = new JRadioButtonMenuItem(mode.toString());
            item.addActionListener(evt -> {
                this.currentMode = mode;
                parseCode();
            });
            parseOutputGroup.add(item);
            parseMenu.add(item);
            this.modeItems.add(item);
        }

        parseMenu.addSeparator();

        this.applyPassesItem = new JCheckBoxMenuItem("Apply passes");
        this.applyPassesItem.addActionListener(evt -> parseCode());
        parseMenu.add(this.applyPassesItem);
        this.convertToCssaItem = new JCheckBoxMenuItem("Convert to CSSA");
        this.convertToCssaItem.addActionListener(evt -> parseCode());
        parseMenu.add(this.convertToCssaItem);
        this.enableZ3Item = new JCheckBoxMenuItem("Enable Z3");
        this.enableZ3Item.setSelected(true);
        this.enableZ3Item.addActionListener(evt -> parseCode());
        parseMenu.add(this.enableZ3Item);
        this.useSharedMemoryItem = new JCheckBoxMenuItem("Use Shared Memory");
        this.useSharedMemoryItem.setSelected(true);
        this.useSharedMemoryItem.addActionListener(evt -> parseCode());
        parseMenu.add(this.useSharedMemoryItem);
        this.subgroupAsWarpFallbackItem = new JCheckBoxMenuItem("Subgroup as warp fallback");
        this.subgroupAsWarpFallbackItem.addActionListener(evt -> parseCode());
        parseMenu.add(this.subgroupAsWarpFallbackItem);
        menuBar.add(parseMenu);

        JMenu debugging = new JMenu("Debugging");
        this.dumpSsaItem = new JCheckBoxMenuItem("Dump SSA Instructions");
        this.dumpSsaItem.addActionListener(evt -> parseCode());
        debugging.add(this.dumpSsaItem);
        this.dumpOutputTypesItem = new JCheckBoxMenuItem("Dump output types");
        this.dumpOutputTypesItem.addActionListener(evt -> parseCode());
        debugging.add(this.dumpOutputTypesItem);
        JMenu loggers = new JMenu("Loggers");

        boolean firstLoggerGroup = true;
        for (List<String> availableLoggers : getAvailableLoggers()) {
            if (firstLoggerGroup) {
                firstLoggerGroup = false;
            } else {
                loggers.add(new JSeparator());
            }

            for (String availableLogger : availableLoggers) {
                String capturedAvailableLogger = availableLogger;
                JCheckBoxMenuItem item = new JCheckBoxMenuItem(availableLogger);
                item.addActionListener(evt -> {
                    if (item.isSelected()) {
                        passesToLog.add(capturedAvailableLogger);
                    } else {
                        passesToLog.remove(capturedAvailableLogger);
                    }
                });
                loggers.add(item);
            }
        }
        debugging.add(loggers);
        menuBar.add(debugging);

        JMenu examples = new JMenu("Examples");
        for (ExampleResource resource : ExampleResource.values()) {
            ExampleResource captured = resource;

            JMenuItem exampleItem = new JMenuItem(captured.getName());
            exampleItem.addActionListener(evt -> {
                setExample(captured);
                parseCode();
            });
            examples.add(exampleItem);
        }
        menuBar.add(examples);

        setJMenuBar(menuBar);

        setLayout(new BorderLayout());

        JPanel toolbar = new JPanel();

        List<DemoMode> values = Arrays.asList(DemoMode.values());
        List<PrintMode> printModes = Arrays.asList(PrintMode.values());

        for (int i = 0; i < values.size(); i++) {
            DemoMode mode = values.get(i);
            JButton button = new JButton(mode.getLabel());
            button.addActionListener(evt -> {
                this.applyPassesItem.setSelected(mode.getApplyPasses());
                this.convertToCssaItem.setSelected(mode.getConvertToCssa());

                int menuIndex = Arrays.asList(PrintMode.values()).indexOf(mode.getMode());
                this.modeItems.get(menuIndex).doClick();
            });
            toolbar.add(button);
        }

        add(toolbar, BorderLayout.NORTH);
        JSplitPane horizontalSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, this.codeTab, this.outputTab);
        add(horizontalSplit, BorderLayout.CENTER);
        JSplitPane verticalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, horizontalSplit, this.diagnosticsScroll);
        verticalSplit.setDividerLocation(600);
        add(verticalSplit);

        setExample(ExampleResource.SIMPLE);
        this.modeItems.get(printModes.indexOf(PrintMode.C_AND_OPENCL)).doClick();

        setSize((int) getPreferredSize().getWidth() + 20, 900);
    }

    private List<List<String>> getAvailableLoggers() {
        List<List<String>> availableLoggers = new ArrayList<>();

        try {
            List<String> basicLoggers = new ArrayList<>();
            availableLoggers.add(basicLoggers);

            HashMap<String, Class<? extends SsaPass>> prePassClasses = PreTypeInferenceSsaRecipeReader
                    .getPassClasses(PreTypeInferenceSsaRecipeReader.DEFAULT_PASS_PACKAGES);
            for (Class<?> clz : prePassClasses.values()) {
                addClassLogger(basicLoggers, clz);
            }

            HashMap<String, Class<? extends PostTypeInferencePass>> postPassClasses = PostTypeInferenceRecipeReader
                    .getPassClasses(PostTypeInferenceRecipeReader.DEFAULT_PASS_PACKAGES);
            for (Class<?> clz : postPassClasses.values()) {
                addClassLogger(basicLoggers, clz);
            }

            Collections.sort(basicLoggers);

            List<String> parallelLoggers = new ArrayList<>();
            availableLoggers.add(parallelLoggers);
            HashMap<String, Class<? extends PostTypeInferencePass>> clPassClasses = PostTypeInferenceRecipeReader
                    .getPassClasses(Arrays.asList(CLRecipes.PASS_PACKAGE));
            for (Class<?> clz : clPassClasses.values()) {
                addClassLogger(parallelLoggers, clz);
            }
            Collections.sort(parallelLoggers);

        } catch (IOException e) {
            SpecsLogs.warn("Error message:\n", e);
        }

        return availableLoggers;
    }

    private void addClassLogger(List<String> availableLoggers, Class<?> clz) {
        try {
            String passName = (String) clz.getField("PASS_NAME").get(null);

            availableLoggers.add(passName);
        } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException | SecurityException e) {
            // Ignore
        }
    }

    private void setExample(ExampleResource example) {
        matlabCodeView.setText(SpecsIo.getResource(example.getMatlabPath()));
        laraCodeView.setText(SpecsIo.getResource(example.getLaraPath()));
    }

    private void parseCode() {
        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream()) {
            PrintStream reportStream = new PrintStream(byteStream);

            CodePrinter printer = this.currentMode.getPrinterClass().newInstance();
            this.outputTab.removeAll();

            this.diagnosticsView.setText("");
            this.diagnosticsScroll.updateUI();

            try {
                addDiagnosticsText("Starting...\n", this.commonStyle);
                long start = System.nanoTime();

                String laraCode = this.laraCodeView.getText();
                String code = this.matlabCodeView.getText();
                DataStore laraSetup = null;

                boolean applyLara = printer.applyLara(this.applyPassesItem.isSelected());

                if (applyLara) {
                    File tempDir = new File("temp");
                    tempDir.mkdir();
                    try {
                        File aspectFile = new File(tempDir, "aspect.lara");
                        SpecsIo.write(aspectFile, laraCode);

                        String functionName = new MatlabParser(languageMode, reportStream)
                                .parse(code)
                                .getMainFunctionTry()
                                .map(FunctionNode::getFunctionName)
                                .orElse("untitled");
                        File matlabFile = new File(tempDir, functionName + ".m");
                        SpecsIo.write(matlabFile, code);
                        File outputFolder = new File(tempDir, "output");
                        outputFolder.mkdir();

                        DataStore defaultSettings = MatlabToCOptionUtils.newDefaultSettings();
                        printer.processSetup(defaultSettings, codeGenSettings);
                        MWeaverRun run = MWeaverUtils.runMWeaver(languageMode,
                                Arrays.asList(matlabFile), aspectFile,
                                Collections.emptyList(),
                                outputFolder,
                                defaultSettings,
                                false);
                        if (run == null) {
                            reportStream.println("Error in LARA aspect.");
                            return;
                        }
                        laraSetup = run.getSettings();

                        code = SpecsIo.read(new File(outputFolder, "mweaver_output/" + functionName + ".m"));
                    } finally {
                        SpecsIo.deleteFolderContents(tempDir);
                        tempDir.delete();

                        new File("Matisse.lara").delete();
                        new File("MatisseOptions.lara").delete();
                    }
                }

                MatlabNode token = new MatlabParser(languageMode, reportStream).parse(code);
                FileNode file = (FileNode) token;

                List<ContentPage> result = printer
                        .getCode(code, laraSetup, file, reportStream,
                                codeGenSettings);

                long end = System.nanoTime();
                addDiagnosticsText("Finished in " + Math.round((end - start) * 1.0e-9 * 100) / 100.0 + " seconds.\n",
                        this.commonStyle);

                for (ContentPage page : result) {
                    JEditorPane astView = new JEditorPane();
                    JScrollPane astScroll = new JScrollPane(astView);

                    astView.setContentType(page.getContentType());
                    astView.setText(page.getCode());

                    outputTab.add(page.getTitle(), astScroll);
                }

            } catch (RuntimeException e) {
                e.printStackTrace(reportStream);
            } finally {
                reportStream.flush();
                String text = byteStream.toString();

                addDiagnosticsText(text, this.errorStyle);
            }
        } catch (Exception e) {
            e.printStackTrace();

            StringWriter writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
        }
    }

    private void addDiagnosticsText(String text, Style style) throws BadLocationException {
        StyledDocument document = this.diagnosticsView.getStyledDocument();
        document.insertString(document.getLength(), text, style);
        this.diagnosticsScroll.updateUI();
    }

    private static String readAll(FileReader reader) throws IOException {
        StringBuilder builder = new StringBuilder();
        try (BufferedReader buffer = new BufferedReader(reader)) {
            String line;
            while ((line = buffer.readLine()) != null) {
                builder.append(line);
                builder.append('\n');
            }
        }

        return builder.toString();
    }
}
