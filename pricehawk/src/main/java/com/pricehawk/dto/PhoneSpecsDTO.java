package com.pricehawk.dto;


public class PhoneSpecsDTO
{

    private String phoneName;
    private String ram;
    private String storage;
    private String camera;
    private String battery;
    private String processor;

    public PhoneSpecsDTO()
    {
    }

    public PhoneSpecsDTO(
            String phoneName,
            String ram,
            String storage,
            String camera,
            String battery,
            String processor
    )
    {
        this.phoneName = phoneName;
        this.ram = ram;
        this.storage = storage;
        this.camera = camera;
        this.battery = battery;
        this.processor = processor;
    }

    public String getPhoneName()
    {
        return phoneName;
    }

    public void setPhoneName(String phoneName)
    {
        this.phoneName = phoneName;
    }

    public String getRam()
    {
        return ram;
    }

    public void setRam(String ram)
    {
        this.ram = ram;
    }

    public String getStorage()
    {
        return storage;
    }

    public void setStorage(String storage)
    {
        this.storage = storage;
    }

    public String getCamera()
    {
        return camera;
    }

    public void setCamera(String camera)
    {
        this.camera = camera;
    }

    public String getBattery()
    {
        return battery;
    }

    public void setBattery(String battery)
    {
        this.battery = battery;
    }

    public String getProcessor()
    {
        return processor;
    }

    public void setProcessor(String processor)
    {
        this.processor = processor;
    }
}