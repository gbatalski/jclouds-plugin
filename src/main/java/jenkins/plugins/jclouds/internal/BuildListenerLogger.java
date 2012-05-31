package jenkins.plugins.jclouds.internal;


import hudson.model.BuildListener;

public class BuildListenerLogger implements org.jclouds.logging.Logger {
   private final BuildListener listener;

   public BuildListenerLogger(BuildListener listener) {
      this.listener = listener;
   }

   public void debug(String message, Object... args) {
      //noop
   }

   public void error(String message, Object... args) {
	listener.fatalError(format(message, args));
   }

   public void error(Throwable throwable, String message, Object... args) {
	listener.fatalError(format(message, args) + ": " + throwable.getCause());
   }

   public String getCategory() {
      return null;
   }

   public void info(String message, Object... args) {
	listener.getLogger().println(format(message, args));
   }

   public boolean isDebugEnabled() {
      return false;
   }

   public boolean isErrorEnabled() {
      return true;
   }

   public boolean isInfoEnabled() {
      return true;
   }

   public boolean isTraceEnabled() {
      return false;
   }

   public boolean isWarnEnabled() {
      return true;
   }

   public void trace(String message, Object... args) {
   }

   public void warn(String message, Object... args) {
	listener.error(format(message, args));
   }

   public void warn(Throwable throwable, String message, Object... args) {
	listener.error(format(message, args) + ": " + throwable.getCause());
   }

    private static String format(String message, Object... args) {
	return args == null ? message : String.format(message, args);

    }

 }