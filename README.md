<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
	xmlns:context="http://www.springframework.org/schema/context"
	xmlns:mvc="http://www.springframework.org/schema/mvc" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns:aop="http://www.springframework.org/schema/aop" xmlns:cache="http://www.springframework.org/schema/cache"
	xmlns:p="http://www.springframework.org/schema/p" xmlns:tx="http://www.springframework.org/schema/tx"
	xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
                http://www.springframework.org/schema/cache http://www.springframework.org/schema/cache/spring-cache.xsd
                http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context.xsd 
				http://www.springframework.org/schema/mvc http://www.springframework.org/schema/mvc/spring-mvc.xsd
				http://www.springframework.org/schema/aop http://www.springframework.org/schema/aop/spring-aop.xsd
				http://www.springframework.org/schema/tx http://www.springframework.org/schema/tx/spring-tx.xsd">

	
	 
    <bean class="org.springframework.beans.factory.config.PropertyPlaceholderConfigurer">
		<property name="systemPropertiesModeName" value="SYSTEM_PROPERTIES_MODE_OVERRIDE" />
		<property name="ignoreResourceNotFound" value="true" />
		<property name="locations">
			<list>
				<value>classpath*:conf/test.properties</value>
				<value>classpath*:/twweb-datasource.properties</value>
			</list>
		</property>
	</bean>
	<context:annotation-config />
	<!-- scan for mappers and let them be autowired -->
	<bean class="org.mybatis.spring.mapper.MapperScannerConfigurer">
		<property name="annotationClass" value="org.springframework.stereotype.Repository"/>
		<property name="basePackage" value="com.***.***.***.mapper" />
		<!-- need below line when there're more than one data sources -->
		<property name="sqlSessionFactoryBeanName" value="sqlSessionFactory"/>
	</bean>
	<context:component-scan base-package="com.***.***.***.service" />
	<tx:annotation-driven transaction-manager="transactionManager" proxy-target-class="true"/>
	
	
	
	<!-- data Source configuration  -->
	<!-- sqlSessionFactory is a auto boxing name if only one data source exists -->
	<bean id="sqlSessionFactory" class="org.mybatis.spring.SqlSessionFactoryBean">
		<property name="dataSource" ref="twLocalDataSource" />
		<property name="mapperLocations" value="classpath*:mappers/**/*.xml" />
	</bean>

<!-- 	<bean id="sqlSessionTemplate" class="org.mybatis.spring.SqlSessionTemplate">
		<constructor-arg index="0" ref="sqlSessionFactory" />
	</bean> -->

	<bean id="transactionManager" class="org.springframework.jdbc.datasource.DataSourceTransactionManager">
		<property name="dataSource" ref="twLocalDataSource" />
	</bean>
	
		<import resource="classpath:/twweb-datasource.xml" />
	<!-- 
	<bean id="twLocalDataSource" class="org.springframework.jndi.JndiObjectFactoryBean">
		<property name="jndiName" value="jdbc/WEB_TW" />
		<property name="resourceRef" value="true" />
	</bean> 
	
	-->
	<!-- Log  configuration  -->
	<aop:config>
		<aop:aspect id="logAspect" ref="logAspect">
			<aop:pointcut expression="execution(* com.***.***.***.service.*.*(..))" id="service"/>
			<aop:around pointcut-ref="service" method="doAround"/>
			<!-- 
			<aop:pointcut expression="execution(* com.***.***.***.controller.*.*(..))" id="controller"/>
			<aop:around pointcut-ref="controller" method="doAround"/>
			
			<aop:pointcut expression="execution(* com.***.***.***.***.retail.rule.service.impl.*.*(..))" id="ruleService"/>
			<aop:around pointcut-ref="ruleService" method="doAround"/>
			
			<aop:pointcut expression="execution(* com.***.***.***.***.retail.builder.*.*(..))" id="builder"/>
			<aop:around pointcut-ref="builder" method="doAround"/>
			 -->
		</aop:aspect>
	</aop:config>
	
	<bean id="logAspect" class="com.***.***.api.framework.interceptor.LoggingInterceptor"/>
	
	<!-- properties  configuration  -->
	<bean id="testMessageSource" class="org.springframework.context.support.ResourceBundleMessageSource">
        <property name="basenames">
        	<list>
            	<value>test</value>
            </list>
        </property>
    </bean>
	<bean id="messageSourceUtil" class="com.***.***.***.util.MessageSourceUtil">
    	<property name="messageSource" ref ="testMessageSource"/>
    </bean>
   
	
</beans>
