package org.apache.flex.utilities.converter.core;

import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.flex.utilities.converter.air.AirConverter;
import org.apache.flex.utilities.converter.deployer.aether.AetherDeployer;
import org.apache.flex.utilities.converter.flash.FlashConverter;
import org.apache.flex.utilities.converter.flex.FlexConverter;
import org.apache.flex.utilities.converter.fontkit.FontkitConverter;
import org.apache.flex.utilities.converter.retrievers.download.DownloadRetriever;
import org.apache.flex.utilities.converter.retrievers.types.PlatformType;
import org.apache.flex.utilities.converter.retrievers.types.SdkType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by christoferdutz on 07.04.15.
 */
public class SdkConverterCLI {

    public static void main(String[] args) throws Exception {
        Options options = new Options();
        options.addOption(OptionBuilder.withDescription("Tells the Converter to download and extract (parts of) an FDK.").create("download"));
        options.addOption(OptionBuilder.withDescription("Tells the Converter to convert an FDK into a mavenized artifacts.").create("convert"));
        options.addOption(OptionBuilder.withDescription("Tells the Converter to deploy mavenized artfifacts to a remote repository.").create("deploy"));

        options.addOption(OptionBuilder.withArgName("version").hasArg().
                withValueSeparator(',').
                withDescription("(Optional and Only valid for download) Version of the FDK which should be downloaded.").
                isRequired(false).
                create("flexVersion"));
        options.addOption(OptionBuilder.withArgName("version(s)").hasArg().
                withValueSeparator(',').
                withDescription("(Optional and Only valid for download) Version(s) of the Adobe Flash SDK which should be downloaded. Multiple versions can be separated by \",\".").
                isRequired(false).
                create("flashVersion"));
        options.addOption(OptionBuilder.withArgName("version(s)").hasArg().
                withValueSeparator(',').
                withDescription("(Optional and Only valid for download) Version(s) of the Adobe Air SDK which should be downloaded. Multiple versions can be separated by \",\".").
                isRequired(false).
                create("airVersion"));
        options.addOption(OptionBuilder.withDescription("(Optional and Only valid for download) If provided, the Converter will also download the Fontkit libraries needed for font encoding.").
                isRequired(false).
                create("fontkit"));
        options.addOption(OptionBuilder.withArgName("platform(s)").hasArg().
                withValueSeparator(',').
                withDescription("(Optional and Only valid for download) Platform the artifacts should be downloaded for. If omitted the platform this process is run on will be used. Valid options are: \"WIN\", \"MAC\" and \"LNX\". Multiple versions can be separated by \",\".").
                isRequired(false).
                create("platform"));
        options.addOption(OptionBuilder.withArgName("dir").hasArg().
                withDescription("(Optional) Directory that the mavenized artifacts will be located in. If omitted, a temporary directory will be used.").
                isRequired(false).
                create("mavenDir"));
        options.addOption(OptionBuilder.withArgName("dir").hasArg().
                withDescription("(Optional) Directory that the FDK will be located in. If omitted, a temporary directory will be used.").
                isRequired(false).
                create("fdkDir"));
        options.addOption(OptionBuilder.withArgName("url").hasArg().
                withDescription("(Optional and only valid for deploy) Url of the remote Maven repository that the generated Maven artifacts should be deployed to.").
                isRequired(false).
                create("repoUrl"));
        options.addOption(OptionBuilder.withArgName("username").hasArg().
                withDescription("(Optional and only valid for deploy) Username used to authenticate on the remote Maven repository that the generated Maven artifacts should be deployed to.").
                isRequired(false).
                create("repoUsername"));
        options.addOption(OptionBuilder.withArgName("password").hasArg().
                withDescription("(Optional and only valid for deploy) Password used to authenticate on the remote Maven repository that the generated Maven artifacts should be deployed to.").
                isRequired(false).
                create("repoPassword"));

        CommandLineParser parser = new BasicParser();
        try {
            CommandLine cmd = parser.parse(options, args);
            if(cmd.getArgList().isEmpty() || cmd.getArgList().contains("help")) {
                printHelp(options);
            }

            // Find out the desired platform(s).
            List<PlatformType> platforms = new ArrayList<PlatformType>();
            String platformParam = cmd.getOptionValue("platform");
            if((platformParam != null) && !platformParam.isEmpty()) {
                String[] platformNames = platformParam.split(",");
                for(String platformName : platformNames) {
                    platforms.add(PlatformType.valueOf(platformName));
                }
            }
            if(platforms.isEmpty()) {
                if(SystemUtils.IS_OS_WINDOWS) {
                    platforms.add(PlatformType.WINDOWS);
                } else if(SystemUtils.IS_OS_MAC) {
                    platforms.add(PlatformType.MAC);
                } else if(SystemUtils.IS_OS_LINUX) {
                    platforms.add(PlatformType.LINUX);
                } else {
                    System.err.println("Unsupported OS type. Provide manually using 'platform' parameter.");
                    System.exit(1);
                }
            }

            // Find out where to download or convert from.
            File fdkDir = cmd.hasOption("fdkDir") ?
                    new File(cmd.getOptionValue("fdkDir")) : getTempDir("FLEX-DOWNLOAD");

            // Find out where to convert to or deploy from.
            File mavenDir = cmd.hasOption("mavenDir") ?
                    new File(cmd.getOptionValue("mavenDir")) : getTempDir("FLEX-MAVEN");

            ////////////////////////////////////////////////////////////////////////////
            // Exectute operations
            ////////////////////////////////////////////////////////////////////////////

            // Handle the downloading of atifacts.
            if(cmd.getArgList().contains("download")) {
                System.out.println("-----------------------------------------------");
                System.out.println("Starting downloads");

                DownloadRetriever retriever = new DownloadRetriever();

                String flexVersion = cmd.getOptionValue("flexVersion", null);
                if(flexVersion != null) {
                    System.out.println("- Downloading Flex SDK version: " + flexVersion +
                            " to directory: " + fdkDir.getAbsolutePath());
                    File fdkDownloadDirectory = retriever.retrieve(SdkType.FLEX, flexVersion);
                    // Unpack the archive to the FDK directory.
                    mergeDirectories(fdkDownloadDirectory, fdkDir);
                }

                String flashVersions = cmd.getOptionValue("flashVersion", "");
                if(!flashVersions.isEmpty()) {
                    for(String flashVersion : flashVersions.split(",")) {
                        System.out.println("- Downloading Flash SDK version: " + flashVersion +
                                " to directory: " + fdkDir.getAbsolutePath());
                        File flashDownloadDiretory = retriever.retrieve(SdkType.FLASH, flashVersion);
                        // Integrate the download into  the FDK directory.
                        mergeDirectories(flashDownloadDiretory, fdkDir);
                    }
                }

                String airVersions = cmd.getOptionValue("airVersion", "");
                if(!airVersions.isEmpty()) {
                    for(String airVersion : airVersions.split(",")) {
                        for(PlatformType platformType : platforms) {
                            System.out.println("- Downloading Air SDK version: " + airVersion +
                                    " and platform " + platformType.name() +
                                    " to directory: " + fdkDir.getAbsolutePath());
                            File airDownloadDirectory = retriever.retrieve(SdkType.AIR, airVersion, platformType);
                            // Integrate the download into the FDK directory.
                            mergeDirectories(airDownloadDirectory, fdkDir);
                        }
                    }
                }

                if(cmd.hasOption("fontkit")) {
                    System.out.println("- Downloading Flex Fontkit libraries" +
                            " to directory: " + fdkDir.getAbsolutePath());
                    File fontkitDownloadDirectory = retriever.retrieve(SdkType.FONTKIT);
                    // Integrate the download into the FDK directory.
                    mergeDirectories(fontkitDownloadDirectory, fdkDir);
                }

                System.out.println("Finished downloads.");
            }

            // Handle the conversion.
            if(cmd.getArgList().contains("convert")) {
                System.out.println("-----------------------------------------------");
                System.out.println("Starting conversion");

                System.out.println("- Converting Flex SDK from " + fdkDir.getAbsolutePath() +
                        " to " + mavenDir.getAbsolutePath());
                FlexConverter flexConverter = new FlexConverter(fdkDir, mavenDir);
                flexConverter.convert();

                System.out.println("- Converting Flash SDKs from " + fdkDir.getAbsolutePath() +
                        " to " + mavenDir.getAbsolutePath());
                FlashConverter flashConverter = new FlashConverter(fdkDir, mavenDir);
                flashConverter.convert();

                System.out.println("- Converting Air SDKs from " + fdkDir.getAbsolutePath() +
                        " to " + mavenDir.getAbsolutePath());
                AirConverter airConverter = new AirConverter(fdkDir, mavenDir);
                airConverter.convert();

                System.out.println("- Converting Fontkit libraries from " + fdkDir.getAbsolutePath() +
                        " to " + mavenDir.getAbsolutePath());
                FontkitConverter fontkitConverter = new FontkitConverter(fdkDir, mavenDir);
                fontkitConverter.convert();

                System.out.println("Finished conversion.");
            }

            // Handle the deployment.
            if(cmd.getArgList().contains("deploy")) {
                System.out.println("-----------------------------------------------");
                System.out.println("Starting deployment");

                if(!cmd.hasOption("repoUrl")) {
                    System.err.println("Parameter 'repoUrl' required for task 'deploy'.");
                    System.exit(1);
                }

                String repoUrl = cmd.getOptionValue("repoUrl");
                String repoUsername = cmd.getOptionValue("repoUsername", null);
                String repoPassword = cmd.getOptionValue("repoPassword", null);

                System.out.println("- Deploying libraries to " + repoUrl + " from " + mavenDir.getAbsolutePath());

                AetherDeployer deployer = new AetherDeployer(mavenDir, repoUrl, repoUsername, repoPassword);
                deployer.deploy();

                System.out.println("Finished deploying.");
            }
            System.out.println("-----------------------------------------------");
        } catch (ParseException e) {
            System.err.println("Parsing failed. Reason: " + e.getMessage());
            printHelp(options);
        }
    }

    protected static void printHelp(Options options) {
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp("[download] [convert] [deploy]", options);
    }

    protected static File getTempDir(String prefix) throws IOException {
        File tempFile = File.createTempFile(prefix, ".TMP");
        tempFile.delete();
        File tempDir = new File(tempFile.getParentFile(),
                tempFile.getName().substring(0, tempFile.getName().length() - 4));
        if(!tempDir.exists()) {
            tempDir.mkdirs();
        }
        return tempDir;
    }

    protected static void mergeDirectories(File sourceDir, File targetDir) throws IOException {
        FileUtils.copyDirectory(sourceDir, targetDir);
    }

}