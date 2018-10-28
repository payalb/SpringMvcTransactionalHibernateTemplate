The  SLF4J serves as a simple facade or abstraction for various logging frameworks, such as java.util.logging, logback and log4j. SLF4J allows the end-user to plug in the desired logging framework at deployment time. Note that SLF4J-enabling your library/application implies the addition of only a single mandatory dependency, namely slf4j-api-x.x.x.jar.

Logback’s architecture is sufficiently generic so as to apply under different circumstances. At present time, logback is divided into three modules, logback-core, logback-classic and logback-access.

The logback-core module lays the groundwork for the other two modules. The logback-classic module can be assimilated to a significantly improved version of log4j. Moreover, logback-classic natively implements the SLF4J API so that you can readily switch back and forth between logback and other logging frameworks such as log4j or java.util.logging (JUL).

The logback-access module integrates with Servlet containers, such as Tomcat and Jetty, to provide HTTP-access log functionality. Note that you could easily build your own module on top of logback-core.”

Spring is using by default Jakarta Commons Logging, but in the blog post Logging Dependencies in Spring from Spring.io it says “if we could turn back the clock and start Spring now as a new project it would use a different logging dependency. Probably the first choice would be the Simple Logging Facade for Java (SLF4J)“

2. Configuration
To use SLF4J with Spring you need to replace the commons-logging dependency with the SLF4J-JCL bridge. Once you have done that, then logging calls from within Spring will be translated into logging calls to the SLF4J API, so if other libraries in your application use that API, then you have a single place to configure and manage logging.

2.1.1. Exclude commons-logging
To switch off commons-logging, the commons-logging dependency mustn’t be present in the classpath at runtime. If you are using maven like me, you can simply exclude it from the spring-context artifact:

  

    <dependency>
    	<groupId>org.springframework</groupId>
    	<artifactId>spring-context</artifactId>
    	<version>${spring.version}</version>
    	<exclusions>
    	   <exclusion>
    		  <groupId>commons-logging</groupId>
    		  <artifactId>commons-logging</artifactId>
    	   </exclusion>
    	</exclusions>			
    </dependency>
  
  
  Because I am using LogBack, which implements SLF4J directly, you will need to depend on two libraries (jcl-over-slf4j and logback):

  

    <!-- LogBack dependencies -->
    <dependency>
    	<groupId>ch.qos.logback</groupId>
    	<artifactId>logback-classic</artifactId>
    	<version>${logback.version}</version>
    </dependency>
    <dependency>                                    
    	<groupId>org.slf4j</groupId>                
    	<artifactId>jcl-over-slf4j</artifactId>     
    	<version>${jcloverslf4j.version}</version>  
    </dependency>
  
  : You might also want to exclude the slf4j-api depedency from other external dependencies, besides Spring, to avoid having more than one version of that API on the classpath.
  
  For Logback configuration through XML, Logback expects a Logback.xml or Logback-test.xml file in the classpath. In a Spring Boot application, you can put the Logback.xml file in the resources folder. If your Logback.xml file is outside the classpath, you need to point to its location using the Logback.configurationFile system property, like this.


-DLogback.configurationFile=/path/to/Logback.xml
1
-DLogback.configurationFile=/path/to/Logback.xml

In a Logback.xml file, all the configuration options are enclosed within the <configuration> root element. In the root element, you can set the debug=true attribute to inspect Logback’s internal status. You can also configure auto scanning of the configuration file by setting the scan=true attribute. When you do so, Logback scans for changes in its configuration file. If Logback finds any changes, Logback automatically reconfigure itself with the changes. When auto scanning is enabled, Logback scans for changes once every minute. You can specify a different scanning period by setting the scanPeriod attribute, with a value specified in units of milliseconds, seconds, minutes or hours, like this.


<configuration debug="true" scan="true" scanPeriod="30 seconds" > 
  ...
</configuration> 

<configuration debug="true" scan="true" scanPeriod="30 seconds" > 
  ...
</configuration> 
The <configuration> root element can contain one or more properties in local scope, each specified using a <property> element. Such a property exists from the point of its definition in the configuration file until the interpretation/execution of the file completes. Configuration options in other parts of the file can access the property using the ${property_name} syntax. Declare properties in the local scope for values that might change in different environments. For example, path to log files, database connections, SMTP server settings, and so on.


<configuration debug="true" scan="true" scanPeriod="30 seconds" > 
  <property name="LOG_PATH" value="logs"/>
  <property name="LOG_ARCHIVE" value="${LOG_PATH}/archive"/> 
 ...
</configuration> 

<configuration debug="true" scan="true" scanPeriod="30 seconds" > 
  <property name="LOG_PATH" value="logs"/>
  <property name="LOG_ARCHIVE" value="${LOG_PATH}/archive"/> 
 ...
</configuration> 
The configuration code above declares two properties, LOG_PATH and LOG_ARCHIVE whose values represent the paths to store log files and archived log files respectively.

At this point, one Logback element worth mentioning is <timestamp>. This element defines a property according to the current date and time – particularly useful when you log to a file. Using this property, you can create a new log file uniquely named by timestamp at each new application launch. The code to declare a timestamp property is this.


<timestamp key="timestamp-by-second" datePattern="yyyyMMdd'T'HHmmss"/>
1
<timestamp key="timestamp-by-second" datePattern="yyyyMMdd'T'HHmmss"/>
In the code above, the datePattern attribute denotes the date pattern used to convert the current time following the conventions defined in SimpleDateFormat.

Next, we’ll look how to use each of the declared properties from different appenders.

Console and File Appenders
You declare one or more appenders with the <appender> element containing the mandatory name and class attributes. The name attribute specifies the appender name that loggers can refer whereas the class attribute specifies the fully qualified name of the appender class. The appender element can contain <layout> or <encoder> elements to define how logging events are transformed. Here is the configuration code to define console and file appenders:


. . .
<appender name="Console-Appender" class="ch.qos.logback.core.ConsoleAppender">
    <layout>
        <pattern>%msg%n</pattern>
    </layout>
</appender>
<appender name="File-Appender" class="ch.qos.logback.core.FileAppender">
    <file>${LOG_PATH}/logfile-${timestamp-by-second}.log</file>
    <encoder>
        <pattern>%msg%n</pattern>
        <outputPatternAsHeader>true</outputPatternAsHeader>
    </encoder>
</appender>

. . .
<appender name="Console-Appender" class="ch.qos.logback.core.ConsoleAppender">
    <layout>
        <pattern>%msg%n</pattern>
    </layout>
</appender>
<appender name="File-Appender" class="ch.qos.logback.core.FileAppender">
    <file>${LOG_PATH}/logfile-${timestamp-by-second}.log</file>
    <encoder>
        <pattern>%msg%n</pattern>
        <outputPatternAsHeader>true</outputPatternAsHeader>
    </encoder>
</appender>
. . .
In the Logback configuration code above:

Line 2 – Line 6: We defined a console appender with the name Console-Appender to use a pattern layout. Note that we haven’t explicitly set the layout, but instead relied on the default Logback value that uses pattern layout.
Line 4: We defined a conversion pattern with the <pattern> element. A conversion pattern is composed of literal text and format control expressions called conversion specifiers. In the code, the %msg conversion specifier outputs the application-supplied message associated with the logging event. The %n conversion specifier outputs the platform dependent line separator characters. You can learn more about pattern layout and conversion specifiers here.
Line 7 – Line 13: We defined a file appender with the name File-Appender. This appender writes to a file defined by the <file> element. Observe how we referred the properties we defined earlier to generate a new log file each time the application starts.
Line 10 – Line 11: We defined an encoder with a pattern. We also used outputPatternAsHeader to insert the pattern used for the log output at the top of log files.
Note: Encoders were introduced in Logback version 0.9.19. Due to the benefits that encoders provide, as explained here, it is recommended to use encoders instead of layouts. As a matter of fact, Logback has removed support for layouts in FileAppender and its sub-classes from version 0.9.19 onwards.

We will now configure an application-specific logger along with the root logger to use the console and appenders, like this.


. . .
<logger name="guru.springframework.blog.logbackxml" level="info">
   <appender-ref ref="File-Appender"/>
</logger>
<root>
    <appender-ref ref="Console-Appender"/>
</root>
. . .
. . .
<logger name="guru.springframework.blog.logbackxml" level="info">
   <appender-ref ref="File-Appender"/>
</logger>
<root>
    <appender-ref ref="Console-Appender"/>
</root>
. . .
In the code above, we defined two loggers. The first logger defined by <logger> configures all loggers under the guru.springframework.blog.logbackxml package to use the file appender. The second one defined by <root> is the root logger configured to use the console appender.

If we run the Log4J2XmlConfTest test class, Log4J 2 will generate log messages and send them to both the console and the file, as shown in this figure.

Console and File Appender Output

Run the test class again. Observe how Logback uses the timestamp property to generate a separate log file based on the specified date pattern.
Log FIles Based on Timestamp from Logback

Rolling File Appender
The rolling file appender supports writing to a file and rolls the file over according to one of your pre-defined policies. The most popular policy is the time-based rolling policy. You can define a time-based policy to perform a rollover once the date/time pattern is no longer applies to the active log file. To learn more about the rolling file appender and its policies, refer to the Logback user manual.

The code to configure a rolling file appender is this.


. . .
<appender name="RollingFile-Appender" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>${LOG_PATH}/rollingfile.log</file>
    <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
        <fileNamePattern>${LOG_ARCHIVE}/rollingfile.%d{yyyy-MM-dd}.log</fileNamePattern>
        <maxHistory>30</maxHistory>
        <totalSizeCap>1MB</totalSizeCap>
    </rollingPolicy>
    <encoder>
        <pattern>%msg%n</pattern>
    </encoder>
</appender>
. . .
. . .
<appender name="RollingFile-Appender" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>${LOG_PATH}/rollingfile.log</file>
    <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
        <fileNamePattern>${LOG_ARCHIVE}/rollingfile.%d{yyyy-MM-dd}.log</fileNamePattern>
        <maxHistory>30</maxHistory>
        <totalSizeCap>1MB</totalSizeCap>
    </rollingPolicy>
    <encoder>
        <pattern>%msg%n</pattern>
    </encoder>
</appender>
. . .
In the code above:

Line 3: The <file> element defines the name of the log file to write to.
Line 4: The <rollingPolicy> element defines a time-based policy.
Line 5: The <fileNamePattern> element defines a file name pattern for archived log files. The rollover period is inferred from the value of <fileNamePattern>, which in the code example is set for daily rolling.
Line 6: The <maxHistory> element sets the maximum number of archive files to keep, before deleting older files asynchronously.
Line 7: The <totalSizeCap&gt element sets the total size of all archive files. Oldest archives are deleted asynchronously when the total size cap is exceeded.
To use the rolling file appender, add the appender reference to the logger declaration, like this.


. . .
<logger name="guru.springframework.blog.logbackxml" level="info">
   <appender-ref ref="File-Appender"/>
    <appender-ref ref="RollingFile-Appender"/>
</logger>
. . .
. . .
<logger name="guru.springframework.blog.logbackxml" level="info">
   <appender-ref ref="File-Appender"/>
    <appender-ref ref="RollingFile-Appender"/>
</logger>
. . .
At this point, when you run the test class, a rolling log file, named rollingfile.log is created under logs. To simulate a rollover, set the system clock one day ahead, and run the test class again. A new rollingfile.log is created under logs and the previous file is archived in the logs/archive folder.
Rolling File Appender Output from Logback

In addition to the time-based rolling policy, you can define a size-based triggering policy. It’s important to understand the difference between rolling and triggering policies. A rolling policy defines WHAT happens when rollover occurs, whereas a triggering policy defines WHEN a rollover should occur. The following code sets a triggering policy to trigger a rollover when the size of a log file exceeds 1 MB.


. . .
<triggeringPolicy class="ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy">
    <maxFileSize>1MB</maxFileSize>
</triggeringPolicy>
.
. . .
<triggeringPolicy class="ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy">
    <maxFileSize>1MB</maxFileSize>
</triggeringPolicy>
. . .
Logback Async Appender
For increased logging performance, we want lower logging latency and higher throughput. Latency is the time required to perform some action or to produce some result. On the other hand, throughput is the number of some actions executed or results produced per unit of time.

To consistently achieve lower latency and higher throughput, Logback supports asynchronous logging through an async appender. Logback executes an async appender in a separate thread to decouple the logging overhead from the thread executing your code.

Using the async appender is incredibly easy. Refer the appender that should be asynchronously invoked within an <appender> element. Then, set the class attribute to the fully qualified name of AsyncAppender, like this.


. . .
<appender name="Async-Appender" class="ch.qos.logback.classic.AsyncAppender">
    <appender-ref ref="RollingFile-Appender"/>
</appender>
. . .

. . .
<appender name="Async-Appender" class="ch.qos.logback.classic.AsyncAppender">
    <appender-ref ref="RollingFile-Appender"/>
</appender>
. . .
Once you define an async appender, you can use it in a logger like any other appender, like this.


. . .
<logger name="guru.springframework.blog.logbackxml" level="info" >
  <appender-ref ref="File-Appender" />
  <appender-ref ref="Async-Appender" />
</logger>
. . .

. . .
<logger name="guru.springframework.blog.logbackxml" level="info" >
  <appender-ref ref="File-Appender" />
  <appender-ref ref="Async-Appender" />
</logger>
. . .
Logback Additivity
To understand Logback additivity, let’s add the configured console appender to the application logger. The logger configuration code is this.


. . .
<logger name="guru.springframework.blog.logbackxml" level="info">
   <appender-ref ref="Console-Appender"/>
   <appender-ref ref="File-Appender"/>
   <appender-ref ref="RollingFile-Appender"/>
</logger>
<root>
    <appender-ref ref="Console-Appender"/>
</root>
.
. . .
<logger name="guru.springframework.blog.logbackxml" level="info">
   <appender-ref ref="Console-Appender"/>
   <appender-ref ref="File-Appender"/>
   <appender-ref ref="RollingFile-Appender"/>
</logger>
<root>
    <appender-ref ref="Console-Appender"/>
</root>
. . .
The console output on running the test class is this.
Console Appender Additivity Output

In the figure above, notice the duplicate output. It’s due to additivity. The appender named Console-Appender is attached to two loggers: root and guru.springframework.blog.Logbackxml. Since root is the ancestor of all loggers, logging request made by guru.springframework.blog.Logbackxml gets output twice. Once by the appender attached to guru.springframework.blog.Logbackxml itself and once by the appender attached to root. You can override this default Logback behavior by setting the additivity flag of a logger to false, like this.


. . .
<logger name="guru.springframework.blog.logbackxml" level="info" additivity="false">
   <appender-ref ref="Console-Appender"/>
   <appender-ref ref="File-Appender"/>
   <appender-ref ref="RollingFile-Appender"/>
</logger>
<root>
    <appender-ref ref="Console-Appender"/>
</root>
. . .

. . .
<logger name="guru.springframework.blog.logbackxml" level="info" additivity="false">
   <appender-ref ref="Console-Appender"/>
   <appender-ref ref="File-Appender"/>
   <appender-ref ref="RollingFile-Appender"/>
</logger>
<root>
    <appender-ref ref="Console-Appender"/>
</root>
. . .
With additivity set to false, Logback will not use Console-Appender of root to log messages.

Although additivity is a convenient feature and is not intended to trip new users, it can be somewhat confusing. I suggest reviewing the Logback manual on the subject.

The complete code of the Logback.xml file is this.

Logback.xml

<?xml version="1.0" encoding="UTF-8"?>
<configuration debug="true" scan="true" scanPeriod="30 seconds">
    <property name="LOG_PATH" value="logs" />
    <property name="LOG_ARCHIVE" value="${LOG_PATH}/archive" />
    <timestamp key="timestamp-by-second" datePattern="yyyyMMdd'T'HHmmss"/>
    <appender name="Console-Appender" class="ch.qos.logback.core.ConsoleAppender">
        <layout>
            <pattern>%msg%n</pattern>
        </layout>
    </appender>
    <appender name="File-Appender" class="ch.qos.logback.core.FileAppender">
        <file>${LOG_PATH}/logfile-${timestamp-by-second}.log</file>
        <encoder>
            <pattern>%msg%n</pattern>
            <outputPatternAsHeader>true</outputPatternAsHeader>
        </encoder>
    </appender>
    <appender name="RollingFile-Appender" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${LOG_PATH}/rollingfile.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>${LOG_ARCHIVE}/rollingfile.log%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>30</maxHistory>
            <totalSizeCap>1KB</totalSizeCap>
        </rollingPolicy>
        <encoder>
            <pattern>%msg%n</pattern>
        </encoder>
    </appender>
    <appender name="Async-Appender" class="ch.qos.logback.classic.AsyncAppender">
        <appender-ref ref="RollingFile-Appender" />
    </appender>

    <logger name="guru.springframework.blog.logbackxml"  level="info" additivity="false">
        <appender-ref ref="Console-Appender" />
        <appender-ref ref="File-Appender" />
        <appender-ref ref="Async-Appender" />
    </logger>
    <root>
        <appender-ref ref="Console-Appender" />
    </root>
</configuration>

<?xml version="1.0" encoding="UTF-8"?>
<configuration debug="true" scan="true" scanPeriod="30 seconds">
    <property name="LOG_PATH" value="logs" />
    <property name="LOG_ARCHIVE" value="${LOG_PATH}/archive" />
    <timestamp key="timestamp-by-second" datePattern="yyyyMMdd'T'HHmmss"/>
    <appender name="Console-Appender" class="ch.qos.logback.core.ConsoleAppender">
        <layout>
            <pattern>%msg%n</pattern>
        </layout>
    </appender>
    <appender name="File-Appender" class="ch.qos.logback.core.FileAppender">
        <file>${LOG_PATH}/logfile-${timestamp-by-second}.log</file>
        <encoder>
            <pattern>%msg%n</pattern>
            <outputPatternAsHeader>true</outputPatternAsHeader>
        </encoder>
    </appender>
    <appender name="RollingFile-Appender" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${LOG_PATH}/rollingfile.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>${LOG_ARCHIVE}/rollingfile.log%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>30</maxHistory>
            <totalSizeCap>1KB</totalSizeCap>
        </rollingPolicy>
        <encoder>
            <pattern>%msg%n</pattern>
        </encoder>
    </appender>
    <appender name="Async-Appender" class="ch.qos.logback.classic.AsyncAppender">
        <appender-ref ref="RollingFile-Appender" />
    </appender>
 
    <logger name="guru.springframework.blog.logbackxml"  level="info" additivity="false">
        <appender-ref ref="Console-Appender" />
        <appender-ref ref="File-Appender" />
        <appender-ref ref="Async-Appender" />
    </logger>
    <root>
        <appender-ref ref="Console-Appender" />
    </root>
</configuration>
Summary
One feature I’d like to see with Logback is the ability to use different appenders at different levels from the same logger. On searching the web, I came to the LOGBACK-625 enhancement issue requesting this feature. One workaround is using a filter inside the appender, as described here. Although not elegant, you can use the approach until the Logback team addresses this enhancement request.

Logger
Loggers are the components that do the heavy work in logging. They capture the logging data and output it to a destination using appenders. The loggers used in an application are typically organized into a hierarchy and a root logger resides at the top of the hierarchy. It is the LoggerContext that is responsible for creating loggers and arranging them in a hierarchy.

Loggers maintains a hierarchical naming rule. As an example, a logger named guru is the parent of the logger, named guru.springframework and the ancestor of the logger, named guru.springframework.blog.
Logger Hierarchy

Apart from logger inheritance, an important logback concept is level inheritance, also referred as effective level. You can assign levels to loggers. Logback supports the TRACE, DEBUG, INFO, WARN and ERROR levels, as shown in this figure.

Log Levels

As you can see in the figure above, TRACE is the lowest level and the level moves up through, DEBUG, INFO, WARN, till ERROR, the highest level. This means that if you set the logger level to WARN, then only the WARN and ERROR level log messages will be displayed and the rest will be ignored.

In addition to the above levels, there are two special levels:

ALL: Turns on all levels.
OFF: Turns off all levels.
If a logger is not assigned a level, then level inheritance comes into play. The logger will inherit the level from its nearest ancestor with an assigned level. If none of the application loggers in the hierarchy have assigned level, the level of the root logger will be inherited. The default level of the root logger is DEBUG.

Note: While developing in your local machine, it is common to set the log level to DEBUG. This will give you detailed log messages for your development use. When deployed to a production environment, it’s typical to set the log level to ERROR. This is to avoid filling your logs with excessive debug information. Also, while logging is very efficient, there is still a cost to system resources.

Appenders
Once you capture logging information through a logger, you need to send it to an output destination. The output destination is called an appender, and it’s attached to the logger. Log4J 2 provides appenders for console, files, remote socket servers, SMTP servers, many popular databases (such as MySQL, PostgreSQL, and Oracle), JMS, remote UNIX Syslog daemons, and more.

Layouts/Encoders
An appender uses a layout to format a log event. A layout, which is an implementation of the Layout interface of log4j-core, transforms a log event to a string. A layout can’t control when log events get written out, and therefore can’t group events into batches. To address the limitations of layouts, logback introduced encoders in version 0.9.19. Encoders, which are implementation of the Encoder interface, transforms an incoming log event into a byte array and writes out the resulting array onto the appropriate output stream. Encoders have total control over the format of the bytes written out. In addition, encoders can control whether (and when) those bytes get written out. 



AsyncAppender acts as a dispatcher to another appender. It buffers ILoggingEvents and dispatches them to another appender asynchronously. This improves the application’s performance because it allows the application to not have to wait for the logging subsystem to complete the action. There is a potential heap memory leak when the buffer builds quicker that it can be drained. Luckily, Logback provides configuration options to address that.

<configuration debug="true" scan="true"
    scanPeriod="150 seconds">
    <property name="LOG_DIR" value="logs" />
    <appender name="CONSOLE"
        class="ch.qos.logback.core.ConsoleAppender" target="System.out">
        <encoder>
            <charset>UTF-8</charset>
            <Pattern>%d %-4relative [%thread] %-5level %logger{35} - %msg%n
            </Pattern>
        </encoder>
    </appender>
 
    <appender name="FILE" class="ch.qos.logback.core.FileAppender">
        <file>${LOG_DIR}/demo.log</file>
        <encoder>
            <charset>UTF-8</charset>
            <Pattern>%d %-4relative [%thread] %-5level %logger{35} - %msg%n
            </Pattern>
        </encoder>
    </appender>
 
    <appender name="EMAIL"
        class="ch.qos.logback.classic.net.SMTPAppender">
        <smtpHost>smtp.gmail.com</smtpHost>
        <smtpPort>587</smtpPort>
        <STARTTLS>true</STARTTLS>
        <username>test@gmail.com</username>
        <password>*****</password>
        <to>test@gmail.com</to>
        <from>test@gmail.com</from>
        <subject>TESTING: %logger{20} - %m</subject>
        <layout class="ch.qos.logback.classic.html.HTMLLayout" />
        <asynchronousSending>false</asynchronousSending>
    </appender>
 
    <appender name="ASYNC_CONSOLE"
        class="ch.qos.logback.classic.AsyncAppender">
        <discardingThreshold>0</discardingThreshold> <!-- default 20, means drop lower event when has 20% capacity remaining -->
        <appender-ref ref="CONSOLE" />
        <queueSize>1</queueSize> <!-- default 256 -->
        <includeCallerData>false</includeCallerData><!-- default false -->
        <neverBlock>true</neverBlock><!-- default false, set to true to cause the 
            Appender not block the application and just drop the messages -->
    </appender>
 
    <appender name="ASYNC_FILE"
        class="ch.qos.logback.classic.AsyncAppender">
        <discardingThreshold>0</discardingThreshold> <!-- default 20, means drop lower event when has 20% capacity remaining -->
        <appender-ref ref="FILE" />
        <queueSize>1</queueSize> <!-- default 256 -->
        <includeCallerData>false</includeCallerData><!-- default false -->
        <neverBlock>false</neverBlock><!-- default false, set to true to cause 
            the Appender not block the application and just drop the messages -->
    </appender>
     
    <appender name="ASYNC_EMAIL"
        class="ch.qos.logback.classic.AsyncAppender">
        <discardingThreshold>0</discardingThreshold> <!-- default 20, means drop lower event when has 20% capacity remaining -->
        <appender-ref ref="EMAIL" />
    </appender>
 
    <logger
        name="jcg.zheng.demo.logbackdemo.component.TestComponent" level="info"
        additivity="false">
        <!-- <appender-ref ref="FILE" /> -->
        <appender-ref ref="ASYNC_FILE" />
        <appender-ref ref="ASYNC_EMAIL" />
    </logger>
 
    <logger
        name="jcg.zheng.demo.logbackdemo.component.TestComponent2"
        level="debug" additivity="false">
        <!-- <appender-ref ref="FILE" /> -->
        <appender-ref ref="ASYNC_FILE" />
 
    </logger>
 
    <root level="error">
        <appender-ref ref="ASYNC_CONSOLE" />
    </root>
</configuration>

  