package com.jakewharton.sdkmanager.internal

import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

import static com.android.SdkConstants.FD_TOOLS
import static com.android.SdkConstants.androidCmdName

interface AndroidCommand {
  int update(String filter);

  static final class Real implements AndroidCommand {
    final Logger log = Logging.getLogger Real
    final File androidExecutable
    final System system

    Real(File sdk, System system) {
      this.system = system
      def toolsDir = new File(sdk, FD_TOOLS)
      androidExecutable = new File(toolsDir, androidCmdName())
      androidExecutable.setExecutable true, false
    }

    @Override int update(String filter) {
      def cmd = generateCommand(filter)
      def process = cmd.execute()

      // Press 'y' and then enter on the license prompt.
      def output = new OutputStreamWriter(process.out)
      output.write("y\n")
      output.close()

      // Pipe the command output to our log.
      def input = new InputStreamReader(process.in)
      def line
      while ((line = input.readLine()) != null) {
        log.debug line
      }

      return process.waitFor()
    }

    def generateCommand(String filter) {
      // -a == all
      // -u == no UI
      // -t == filter
      // -s == no HTTPS
      // --proxy-host == hostname of a proxy server
      // --proxy-port == port of a proxy server
      def proxyHost = system.property("http.proxyHost");
      def proxyPort = system.property("http.proxyPort");
      def result = [androidExecutable.absolutePath, 'update', 'sdk', '-a', '-u'];
      if (proxyHost != null && proxyPort != null) {
        result += ['--proxy-host', proxyHost, '--proxy-port', proxyPort]
      }
      result += ['-t', filter]
      return result;
    }
  }
}
