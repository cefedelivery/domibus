# How to use Domibus with Docker

You can run Domibus very easily using docker. There are several ways to accomplish this. These are (in order of complexity):

- _"Have everything automatically done for me"_. See Section **1. The Fastest Way** (recommended).
- _"Let me see how this docker thing works from the inside"_ or _"I want to customize my Domibus Docker file"_ : See Section **2. Build a docker image**.

These are alternative ways to do the same thing, you do not need to do them both.

You do need to have docker in your machine. See the [documentation](https://docs.docker.com/installation/) on how to do this.

----
## 1. The Fastest Way

Docker Compose allows you to link a Domibus container (running on Tomcat 8) to a MySQL container in a few minutes. This method requires that you install [Docker Compose](https://docs.docker.com/compose/install/).

Follow these steps:

    `cd tomcat-mysql-compose`
    `sudo docker-compose up`

After a few seconds you should have your Domibus running and listening on port `8080`.

Check that everything works with

	http://localhost:8080/domibus/home 

You should see the Domibus login page:

	user: admin
	password: 123456

What you have done with this method is download images from [Domibus on Tomcat](https://hub.docker.com/r/fiware/domibus-tomcat/)
and [MySQL](https://hub.docker.com/r/fiware/domibus-mysql/) from the public repository of images called [Docker Hub](https://hub.docker.com/).
Then you have created two containers based on both images.

If you want to stop the scenario you have to press Control+C on the terminal where docker-compose is running.
Note that you will lose any data that was being used in Domibus using this method.

----
## 2. Build a docker image

Building an image gives more control on what is happening within the Domibus container. Only use this method if you are familiar with building docker images or really need to change how this image is built. For most purposes you probably don't need to build an image, only deploy a container based on one already built for you (which is covered in section 1).

Steps:

1. Modify the two dockerfiles located in tomcat/ and mysql/ folders to your liking
2. Build new images:

	`cd tomcat`
	`docker build -t domibus-tomcat .`

	`cd ../mysql`
    `docker build -t domibus-mysql .`

The parameter `-t domibus-tomcat` in the `docker build` command gives the image a name.
This name could be anything, or even include an organization like `-t org/domibus-tomcat`.
This name is later used to run the container based on the image.
5. You also have to modify the provided `docker-compose.yml` file to use the newly created images instead
of the public ones from docker hub.
4. Run Domibus using docker-compose

    `sudo docker-compose up`.

Check that everything works with

	http://localhost:8080/domibus/home 

You should see the Domibus login page:

	user: admin
	password: 123456

If you want to know more about images and the building process you can find it in [Docker's documentation](https://docs.docker.com/userguide/dockerimages/).

## 4. Other info

Things to keep in mind while working with docker containers and Domibus.

### 4.1 Data persistence
Everything you do with Domibus when dockerized is non-persistent. *You will lose all your data* if you turn off the MySQL container. This will happen with either method presented in this README.

### 4.2 Using `sudo`

If you do not want to have to use `sudo` follow [these instructions](http://askubuntu.com/questions/477551/how-can-i-use-docker-without-sudo).
