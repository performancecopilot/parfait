package com.custardsource.parfait.cxf;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.custardsource.parfait.timing.EventTimer;
import com.custardsource.parfait.timing.Timeable;

@Path("/restdemo/")
@Produces("text/plain")
public class RestDemo implements Timeable {
	@GET
    @Path("/sayhello/")
    public String sayHello() {
        return "Hi there!";
    }

	@GET
    @Path("/snooze/")
    public String sleepyTime() throws InterruptedException {
        Thread.sleep(2000);
        return "ZZZZZ";
    }

	@Override
	public void setEventTimer(EventTimer timer) {
	}
}