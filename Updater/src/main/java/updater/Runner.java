/* =================================================
 * Author: Cristian Capraro
 * Updater program for BookShelf App
 * Date: 23-09-2022
 * -------------------------------------------------
 * This is the main class of this module
 * args[0] -> installed version
 * args[1] -> parent pid
 * ================================================= */

package updater;

import javax.net.ssl.SSLHandshakeException;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.HashMap;
import java.util.concurrent.Semaphore;

import static java.lang.System.exit;
import static java.lang.System.out;

public class Runner {
	//Mirror
	private final static String mirrorLink = "https://bookshelf.rootlet.it/get-version.txt";
	private static int progressValue = 0;
	private static final Semaphore sem = new Semaphore(1); //Semaphore for progressValue

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		if(args.length != 2){
			//Missing requirements
			out.println("Requirements error - This updater need to retrieve the BookShelf version as argument");
			JOptionPane.showMessageDialog(null, "Il programma non deve essere eseguito dall'utente");
			exit(255);
		}

		if(!args[0].matches("[0-9]\\.[0-9]\\.[0-9]")){
			//Matching string
			out.println("Params error - This updater need to retrieve the BookShelf version as argument");
			JOptionPane.showMessageDialog(null, "Il programma non deve essere eseguito dall'utente");
			exit(254);

		}
		if(!checkInternet()){
			//Cannot connect to the Internet
			out.println("Net error - Internet connection currently unavailable.");
			JOptionPane.showMessageDialog(null, "Connessione ad Internet assente.\nImpossibile verificare gli aggiornamenti");
			exit(253);
		}


		HashMap<String, Integer> InstalledVersion = new HashMap<>();
		InstalledVersion.put("Major", Integer.parseInt(args[0].split("\\.")[0]));
		InstalledVersion.put("Minor", Integer.parseInt(args[0].split("\\.")[1]));
		InstalledVersion.put("Patch", Integer.parseInt(args[0].split("\\.")[2]));

		//Obtaining information
		Object[] data = checkMirror();
		HashMap<String, Integer> RemoteVersion = (HashMap<String, Integer>) data[0];
		URI ProgramSource = (URI) data[1];

		if(RemoteVersion.get("Major").equals(InstalledVersion.get("Major"))){
			if(RemoteVersion.get("Minor").equals(InstalledVersion.get("Minor"))){
				if(RemoteVersion.get("Patch").equals(InstalledVersion.get("Patch"))){
					JOptionPane.showMessageDialog(null, "Non sono disponibili ulteriori aggiornamenti\nUltima versione installata\nPremere OK per uscire");
					exit(0);
				}else{
					int res = JOptionPane.showOptionDialog(null, "Aggiornamento (patch/sicurezza)\nPremere OK per continuare", "Aggiornamento disponibile", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null);
					if(res == JOptionPane.CANCEL_OPTION) exit(0);
				}
			}else{
				int res = JOptionPane.showOptionDialog(null, "Aggiornamento (minor/funzioni)\nPremere OK per continuare", "Aggiornamento disponibile", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null);
				if(res == JOptionPane.CANCEL_OPTION) exit(0);
			}
		}else{
			int res = JOptionPane.showOptionDialog(null, "Aggiornamento importante! (major/critico)\nPremere OK per continuare", "Aggiornamento disponibile", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null);
			if(res == JOptionPane.CANCEL_OPTION) exit(0);
		}

		try{
			Runnable updatePanel = () -> {
				JFrame pane = new JFrame("Aggiornamento in corso...", null);
				pane.setLayout(new GridBagLayout());
				GridBagConstraints c = new GridBagConstraints();
				pane.setSize(500, 300);
				JLabel label = new JLabel("Attendere prego...");
				label.setSize(200, 40);
				c.fill = GridBagConstraints.HORIZONTAL;
				c.weightx = 0.5;
				c.gridx = 1;
				c.gridy = 0;
				pane.add(label ,c);
				JProgressBar progressBar = new JProgressBar(0, 100);
				progressBar.setSize(200, 60);
				progressBar.setStringPainted(true);
				label.setLabelFor(progressBar);
				progressBar.setValue(20);
				c.fill = GridBagConstraints.HORIZONTAL;
				c.ipady = 40;
				c.weightx = 0.0;
				c.gridwidth = 3;
				c.gridx = 0;
				c.gridy = 1;
				pane.add(progressBar, c);

				//Progress bar thread
				Runnable pgBarSet = () -> {
					while(getProgressValue() != 100){
						progressBar.setValue(getProgressValue());
						try {
							sem.acquire(); //Semaphore use
						} catch (InterruptedException ignored) {}
					}
					try{
						Thread.sleep(5000);
					}catch (Exception ignored){ }
					JOptionPane.showMessageDialog(null, "Aggiornamento completato\nPremere OK per chiudere");
					exit(0);
				};

				pane.setResizable(false);
				pane.setLocationRelativeTo(null);
				pane.setVisible(true);
				new Thread(pgBarSet).start();
			};
			new Thread(updatePanel).start();

			progressValue = 0;
			sem.release();
			//Close App
			CloseBookShelf(Long.parseLong(args[1]));
			progressValue = 15;
			sem.release();

			downloadFile(ProgramSource.toURL());
			progressValue = 50;
			sem.release();
			ProcessBuilder Uninstall = new ProcessBuilder(".\\Uninstall.exe");
			Process uninstallPS = Uninstall.start();
			Thread.sleep(15000); //Sleep for 15 seconds
			progressValue = 60;
			sem.release();

			ProcessBuilder Install = new ProcessBuilder("\""+System.getProperty("user.home")+File.separator+"BookShelf-Installer.exe"+"\"");
			Process installPS = Install.start();
			progressValue = 70;
			sem.release();
			installPS.waitFor();
			Thread.sleep(15000); //Sleep for 15 seconds
			progressValue = 99;
			sem.release();
			Thread.sleep(1000); //Delay 1s
			progressValue = 100;
			sem.release();
		}catch (IOException exc){
			JOptionPane.showMessageDialog(null, "Errore. Si prega di riprovare tra qualche istante.");
			exit(239);
		} catch (Exception e) {
			exit(238);
		}

	}

	private static void downloadFile(URL url){
		File installer;
		//Delete existing old installer
		if((installer = new File(System.getProperty("user.home")+File.separator+"BookShelf-Installer.exe")).exists()){
			installer.delete();
		}
		try(FileOutputStream fileOutputStream = new FileOutputStream(System.getProperty("user.home")+File.separator+"BookShelf-Installer.exe")){
			ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
			fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
		}catch (IOException e){
			exit(240);
		}
	}

	private static void CloseBookShelf(long pid){
		ProcessHandle.of(pid).ifPresent(ProcessHandle::destroy);
	}

	private static boolean checkInternet(){
		final int pingTimeout = 30000; //millis
		try{
			InetAddress toPing = InetAddress.getByName("www.google.it");
			return toPing.isReachable(pingTimeout);
		}catch (IOException exc){
			return false;
		}
	}

	private static Object[] checkMirror(){
		Object[] output = new Object[2];
		URI mirror = URI.create(mirrorLink);
		String args = "";
		URI Source = null;
		try(BufferedReader in = new BufferedReader(new InputStreamReader(mirror.toURL().openConnection().getInputStream()))){
			args = in.readLine();
			Source = new URI(in.readLine());
		}catch (SSLHandshakeException e){
			out.println("Errore Cert - Per precauzione, al momento, gli aggiornamenti rimarranno disabilitati.");
			exit(122);

		}
		catch (URISyntaxException | IOException e) {
			exit(252);
		}
		out.println("Found -> "+args);
		out.println("Source -> "+Source);
		HashMap<String, Integer> RemoteVersion = new HashMap<>();
		RemoteVersion.put("Major", Integer.parseInt(args.split("\\.")[0]));
		RemoteVersion.put("Minor", Integer.parseInt(args.split("\\.")[1]));
		RemoteVersion.put("Patch", Integer.parseInt(args.split("\\.")[2]));
		output[0] = RemoteVersion;
		output[1] = Source;
		return output;
	}

	public static int getProgressValue() {
		return progressValue;
	}
}
