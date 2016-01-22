<a href="http://icclab.github.io/cyclops" target="_blank"><img align="middle" src="http://icclab.github.io/cyclops/assets/images/logo_big.png"></img></a>

## User Data Records microservice
UDR Service is one of the micro services as part of <a href="http://icclab.github.io/cyclops" target="_blank">CYCLOPS - A Rating, Charging & Billing solution</a> for cloud being developed by <a href="http://blog.zhaw.ch/icclab/">InIT Cloud Computing Lab</a> at <a href="http://www.zhaw.ch">ZHAW</a>. The service collects the usage data from a source (OpenStack, CloudStack, SaaS, PaaS, etc ..), transforms the data and persists it in a Time Series Database. This harmonised data is exposed through REST APIs. The harmonised usage data from UDR Service is used by other micro services (Rating & Charging Service & Billing Service) to process and generate a bill for the end user as per the usage made.

Salient Features
  * Quicker access to usage details through harmonised data
  * Persisting usage data as time series
  * APIs to insert usage data from external Paas/SaaS
  
### Download
     $ git clone https://github.com/icclab/cyclops-udr.git
### Installation
     $ cd cyclops-udr/install
     $ chmod +x ./*
#### For OpenStack metering
     $ bash install_openstack_prereq.sh
#### For CloudStack metering
     $ bash install_cloudstack_prereq.sh
#### For Event based metering
     $ bash install_events_prereq.sh

<b>Note</b>: Currently, it's not possible to have UDR deployment of OpenStack, CloudStack and Event based metering at the same time, please select just one of them.

#### Configuration
 * At the end of the installation process you will be asked for your deployment credentials and to modify any configuration parameters, **please do not ignore this step.**
 * If there is a need to update your configuration, you can find it stored here cyclops-udr/src/main/webapp/WEB-INF/configuration.txt

### Deployment
     $ bash deploy_udr.sh

### Documentation
  Visit the <a href="https://github.com/icclab/cyclops-udr/wiki">Wiki</a> for detailed explanation and API reference guide.

### Cyclops architecture
<img align="middle" src="http://blog.zhaw.ch/icclab/files/2013/05/overall_architecture.png" alt="CYCLOPS Architecture" height="500" width="600"></img>

#### UDR microservice
<img align="middle" src="http://blog.zhaw.ch/icclab/files/2015/06/UDRGeneratorService.png" alt="UDR Service Architecture" height="400" width="700"></img>
  
### Bugs and issues
  To report any bugs or issues, please use <a href="https://github.com/icclab/cyclops-udr/issues">Github Issues</a>
  
### Communication
  * Email: icclab-rcb-cyclops[at]dornbirn[dot]zhaw[dot]ch
  * Website: <a href="http://icclab.github.io/cyclops" target="_blank">icclab.github.io/cyclops</a>
  * Blog: <a href="http://blog.zhaw.ch/icclab" target="_blank">http://blog.zhaw.ch/icclab</a>
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
