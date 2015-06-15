## User Data Records Service - A <a href="http://icclab.github.io/cyclops">CYCLOPS Micro Service</a>
UDR Service is one of the micro services as part of CYCLOPS - A Rating, Charging  & Billing solution for cloud being developed by <a href="http://blog.zhaw.ch/icclab/">InIT Cloud Computing Lab</a> at <a href="http://www.zhaw.ch">ZHAW</a>. The service collects the usage data from a source (OpenStack,cloudstack, SaaS, PaaS, etc ..), transforms the data and persists it in a Time Series Database. This harmonized data is exposed through REST APIs. The harmonized usage data from UDR Service is used by other micr services (Rating & Charging Service & Billing Service) to process and generate a bill for the end user as per the usage made.

Salient Features
  * Quicker access to usage details through harmonized data
  * Persisting usage data as time series
  * APIs to insert usage data from external Paas/SaaS
  * Rich visualization through Grafana
  
### Getting started
#### Installation
     $ git clone https://github.com/icclab/cyclops-udr.git
     $ cd cyclops-udr/install
     $ chmod +x ./*
     $ bash install.sh xxx.xxx.xxx.xxx [IP of the machine]

<b>Note</b>: Immediately change the default username & password created for Tomcat7 & InfluxDB

#### Configuration
 * CD to cyclops-udr/web/WEB-INF/configuration.txt
 * Add the Keystone details (URL, username, password, domain name, project name), Telemetry details (URL)
 * Add InfluxDB details (URL, username, password). As part of the installation script, InfluxDB is installed on localhost.
 * Restart Tomcat7 [$ sudo service tomcat7 restart]

### Architecture
#### * CYCLOPS Rating Charging & Billing Framework
<img align="middle" src="http://blog.zhaw.ch/icclab/files/2013/05/overall_architecture.png" alt="CYCLOPS Architecture" height="500" width="600"></img>

#### * Usage Data Record Micro Service
<img align="middle" src="http://blog.zhaw.ch/icclab/files/2015/06/UDRGeneratorService.png" alt="UDR Service Architecture" height="400" width="700"></img>


#### Full documentation
  * Access <a href="http://icclab.github.io/cyclops/javadoc/udrservice/">Javadoc</a>
  * Visit the <a href="https://github.com/icclab/cyclops-udr/wiki">wiki</a> for detailed explanation.
  
### Bugs and issues
  To report any bugs or issues, please use <a href="https://github.com/icclab/cyclops-udr/issues">Github Issues</a>

#### Components & Libraries
  * <a href="https://www.sensuapp.org">Sensu</a>
  * <a href="https://www.influxdb.com">InfluxDB</a>
  * <a href="https://tomcat.apache.org">Tomcat7</a>
  * <a href="https://httpd.apache.org">Apache2</a> 
  * <a href="https://restlet.com">RESTLET</a> 
  
### Communication
  * Issues/Ideas/Suggestions : <a href="https://github.com/icclab/cyclops-udr/issues">GitHub Issue</a>
  * Email : <a href="http://blog.zhaw.ch/icclab/srikanta-patanjali/">Srikanta</a> (pata at zhaw[dot]ch) or <a href="http://blog.zhaw.ch/icclab/piyush_harsh/">Piyush</a> (harh at zhaw[dot]ch)
  * Website : http://blog.zhaw.ch/icclab/ 
  * Tweet us @<a href="https://twitter.com/ICC_Lab">ICC_Lab</a>
   
### Developed @
<img src="http://blog.zhaw.ch/icclab/files/2014/04/icclab_logo.png" alt="ICC Lab" height="180" width="620"></img>

### License
 
      Licensed under the Apache License, Version 2.0 (the "License"); you may
      not use this file except in compliance with the License. You may obtain
      a copy of the License at
 
           http://www.apache.org/licenses/LICENSE-2.0
 
      Unless required by applicable law or agreed to in writing, software
      distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
      WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
      License for the specific language governing permissions and limitations
      under the License.
