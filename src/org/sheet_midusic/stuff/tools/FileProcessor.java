package org.sheet_midusic.stuff.tools;

import org.jm.midi.SMF;
import org.klesun_model.Explain;
import org.sheet_midusic.staff.staff_panel.SheetMusic;
import org.sheet_midusic.staff.staff_panel.SheetMusicPanel;
import org.sheet_midusic.stuff.main.Main;
import org.sheet_midusic.staff.Staff;
import org.klesun_model.IModel;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;

import org.sheet_midusic.stuff.tools.jmusic_integration.JMusicIntegration;
import org.json.JSONException;
import org.json.JSONObject;
import org.sheet_midusic.stuff.Midi.SimpleMidiParser;

import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.function.Function;

public class FileProcessor {

	// TODO: make folder of project default path
	private static JFileChooser fileChooser = new JFileChooser("/home/klesun/yuzefa_git/a_opuses_json/");
	
	public static Explain savePNG (SheetMusicPanel sheetMusicPanel)
	{
		return makeSaveFileDialog("png", "PNG images").ifSuccess(f ->
		{
			BufferedImage img = new BufferedImage(sheetMusicPanel.getWidth(), sheetMusicPanel.getHeight(), BufferedImage.TYPE_INT_ARGB);
			sheetMusicPanel.paintComponent(img.getGraphics());

			return Explain.tryException(() -> ImageIO.write(img, "png", f));
		});
	}

//	public static Explain<File> saveStoryspace(BlockSpace blockSpace) {
//		return makeSaveFileDialog("bs.json", "BlockSpace Project Json Data")
//				.ifSuccess(f -> saveModel(f, blockSpace));
//	}

	public static Explain saveMusicPanel(SheetMusicPanel sheetMusicPanel) {

		return makeSaveFileDialog("midi.json", "Json Midi-music data").ifSuccess(f -> {
//			staff.getParentSheet().getParentBlock().setTitle(f.getName());
			return saveModel(f, sheetMusicPanel.sheetMusic); // TODO: use messages when fail
		});
	}

	public static Explain saveMidi(SheetMusicPanel sheetMusicPanel) {
		return makeSaveFileDialog("mid", "MIDI binary data file")
			.ifSuccess(f -> writeSheetMusicMidi(sheetMusicPanel.sheetMusic, f));
	}

	private static Explain writeSheetMusicMidi(SheetMusic sheetMusic, File f)
	{
		try {
			SMF smf = SimpleMidiParser.sheetMusicToSmf(sheetMusic);
			OutputStream os = new FileOutputStream(f);
			smf.write(os);
			return new Explain(true);
		} catch (IOException exc) {
			return new Explain("Failed to write Staff to file", exc);
		}
	}

//	public static Explain openStoryspace(File f, BlockSpace blockSpace) {
//		Main.window.setTitle(f.getAbsolutePath());
//		return openModel(f, blockSpace);
//	}

	public static Explain openStaff(SheetMusicPanel sheetMusicPanel) {
		fileChooser.resetChoosableFileFilters();
		fileChooser.setFileFilter(new FileFilter() {
			public boolean accept(File f) {
				return f.getAbsolutePath().endsWith(".midi.json") || f.isDirectory();
			}
			public String getDescription() {
				return "Json Midi-music data";
			}
		});
		if (fileChooser.showOpenDialog(Main.window) == JFileChooser.APPROVE_OPTION) {
			File f = fileChooser.getSelectedFile();
//			staff.getParentSheet().getParentBlock().setTitle(f.getName());

			return openModel(f, sheetMusicPanel.sheetMusic);
		} else {
			return new Explain(false, "you changed your mind, why?");
		}
	}

	// with rounding
	public static Explain openJMusic2(Staff staff) {
		return openJMusicAndPerform(staff, new JMusicIntegration(staff)::fillFromJm2);
	}

	// TODO: we'll need this just untill i get opening MIDI into MIDIana
	public static Explain openJMusic(Staff staff) {
		return openJMusicAndPerform(staff, new JMusicIntegration(staff)::fillFromJm);
	}

	private static Explain openJMusicAndPerform(Staff staff, Function<JSONObject, Explain> fillStaffLambda) {
		fileChooser.resetChoosableFileFilters();
		fileChooser.setFileFilter(new FileFilter() {
			public boolean accept(File f) { return f.getAbsolutePath().endsWith(".jm.json") || f.isDirectory(); }
			public String getDescription() { return "Json JMusic data"; }
		});
		if (fileChooser.showOpenDialog(Main.window) == JFileChooser.APPROVE_OPTION) {
			File f = fileChooser.getSelectedFile();
//			staff.clearStan().getParentSheet().getParentBlock().setTitle(f.getName());

			Explain<JSONObject> jsExplain = openJsonFile(f);
			return jsExplain.isSuccess() ? fillStaffLambda.apply(jsExplain.getData()) : jsExplain;
		} else {
			return new Explain(false, "you changed your mind, why?");
		}
	}

	private static Explain openModel(File f, IModel model)
	{
		return openJsonFile(f).ifSuccess(js ->
		{
			if (js.has(model.getClass().getSimpleName())) {
				JSONObject modelJs = js.getJSONObject(model.getClass().getSimpleName());

				try {
					model.reconstructFromJson(modelJs);
					return new Explain(true);
				} catch (JSONException exc) {
					return new Explain(false, "Failed to Open Model, some Json error: " + describeException(exc));
				}
			} else {
				return new Explain(false, "File you provided does not have [" + model.getClass().getSimpleName() + "] key in main body, " + "" +
					"only " + Arrays.toString(JSONObject.getNames(js)) + "]");
			}
		});
	}

	private static String describeException(Exception exc)
	{
		String traceString = Fp.traceDiff(exc, new Throwable());
		return exc.getClass().getSimpleName() + " " + exc.getMessage() + "\n\n" + traceString;
	}

	private static Explain<JSONObject> openJsonFile(File f) {
		return readTextFromFile(f).ifSuccess(jsString -> {
			try {
				JSONObject jsonParse = new JSONObject(jsString);
				return new Explain(jsonParse);
			} catch (JSONException exc) {
				return new Explain(false, "Failed to parse json - [" + exc.getMessage() + "]");
			}
		});
	}

	private static Explain<File> makeSaveFileDialog(String ext, String description) {
		fileChooser.resetChoosableFileFilters();
		fileChooser.setFileFilter(new FileFilter() {
			public boolean accept(File f) {
				return f.getAbsolutePath().endsWith("." + ext) || f.isDirectory();
			}
			public String getDescription() {
				return description;
			}
		});

		int rVal = fileChooser.showSaveDialog(Main.window);
		if (rVal == JFileChooser.APPROVE_OPTION) {
			File fn = fileChooser.getSelectedFile();
			if (!fileChooser.getFileFilter().accept(fn)) { fn = new File(fn + "." + ext); }
			return new Explain<>(fn);
		} else { return new Explain<>(false, "you changed your mind, why?"); }
	}

	// i made it public only for Logger.fatal()
	public static Explain saveModel(File f, IModel model)
	{
		JSONObject js = new JSONObject("{}")
			.put(model.getClass().getSimpleName(), model.getJsonRepresentation());

		return writeTextToFile(js.toString(2), f);
	}

	private static Explain writeTextToFile(String text, File f)
	{
		try {
			PrintWriter out = new PrintWriter(f);
			out.println(text);
			out.close();
		} catch (IOException exc) {
			return new Explain("Failed to write text to file ", exc);
		}

		Main.window.setTitle(f.getAbsolutePath());
		return new Explain(true);
	}

	private static Explain<String> readTextFromFile(File f)
	{
		try {
			String text = new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
			return new Explain<>(text);
		} catch (IOException exc) {
			return new Explain<>("Failed to read text from file ", exc);
		}
	}
}
