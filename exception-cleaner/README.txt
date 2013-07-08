This is an exception stacktrace cleaner for the logback framework
The layout itself inherits from the PatternLayout (i'm sure most of you use this layout anyway), so you don't really have to reconfigure your PatternLayout for this
Add <include> tags for each package prefix you want to use. this will filter out everything from between the included package, but not before or after
(there are flags for that, but i don't think it's really useful to filter out the "origin" and "destination" of your stacktrace anyway)
if you leave out the <include> tags the layout will behave like a normal PatternLayout

simple usage: in logback.xml configure your appender like so:

	<appender name="Console" class="ch.qos.logback.core.ConsoleAppender">
		<encoder class="ch.qos.logback.core.encoder.LayoutWrappingEncoder">
			<layout class="com.ravello.utils.logs.ExceptionFilteredPatternLayout">
				<pattern>${your_pattern}</pattern>
 				<include>com.ravello</include>
				<include>another.packag</include>
			</layout>
		</encoder>
	</appender>
	
	