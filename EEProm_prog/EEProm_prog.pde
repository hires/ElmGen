/*
 * EEPROM Programmer for 24LC32A
 *
 * By: Andrew Kilpatrick
 *
 * Analog 4 - SDA
 * Analog 5 - SCL
 * 
 */
#include "Wire.h"
#define led 13

#define CMD_READ_DATA 0x01
#define CMD_WRITE_DATA 0x02
#define RET_READ_DATA 0x81
#define RET_WRITE_ACK 0x82

#define SOH 0x01
#define EOT 0x04
#define ESC 0x1b
#define ESC_SOH 0x78
#define ESC_EOT 0x79
#define ESC_ESC 0x7a

#define RX_IDLE 0
#define RX_CMD 1
#define RX_DATA_LEN 2
#define RX_DATA 3
#define RX_EOT 4
#define MAX_DATA 32 

byte rxState;
byte cmd;
byte dataLen;
byte rxEscFlag;
byte rxDataCount;
byte msgData[MAX_DATA];

// setup
void setup() {
  pinMode(led, OUTPUT); 
  digitalWrite(led, LOW);
  
  // init some data
  for(int i = 0; i < MAX_DATA; i ++) {
     msgData[i] = 0;
  }
  
  rxState = RX_IDLE;
  Serial.begin(9600);
  Wire.begin();
}

// main run loop
void loop() {
  if(Serial.available() > 0) {
    digitalWrite(led, HIGH);
    handleRxByte(Serial.read());
    digitalWrite(led, LOW);
  }
}

// handle a byte received from the host
void handleRxByte(byte data) {
    byte rxByte = data;
    
    if(rxByte == SOH) {
      cmd = 0;
      dataLen = 0;
      rxEscFlag = 0;
      rxDataCount = 0;
      rxState = RX_CMD;
      return;
    }
    if(rxByte == ESC) {
      rxEscFlag = 1;
      return; 
    }
    if(rxEscFlag == 1) {
      if(rxByte == ESC_SOH) {
        rxByte = SOH;
      }
      else if(rxByte == ESC_EOT) {
        rxByte = EOT;
      }
      else if(rxByte == ESC_ESC) {
        rxByte = ESC;
      }
      else {
        rxState = RX_IDLE;
        return;
      }
      rxEscFlag = 0; 
    }
    if(rxState == RX_CMD) {
       cmd = rxByte;
       rxState = RX_DATA_LEN;
       return;
    }
    if(rxState == RX_DATA_LEN) {
       dataLen = rxByte;
       if(dataLen > MAX_DATA) {
          rxState = RX_IDLE;
          return; 
       }
       if(dataLen == 0) {
         rxState = RX_EOT;
       }
       else {
         rxState = RX_DATA;
       }
       return;
    }
    if(rxState == RX_DATA) {
       msgData[rxDataCount] = rxByte;
       rxDataCount ++;
       if(rxDataCount == dataLen) {
          rxState = RX_EOT;
       }
       return; 
    }
    if(rxState == RX_EOT) {
       rxState = RX_IDLE; 
       if(rxByte == EOT) {
          processRxMsg();
       }
    }
}

// processes a complete received message
void processRxMsg() {
   if(cmd == CMD_READ_DATA) {
      if(dataLen != 3) return;
      int start = (msgData[0] << 8) | msgData[1];
      int length = msgData[2];
      if(length > 16) return;
      // get some data into the msgData buffer
      i2c_eeprom_read_buffer(start, msgData + 3, length);
      // send the data back to the host
      cmd = RET_READ_DATA;
      dataLen = length + 3;
      sendMsg();
   }
   else if(cmd == CMD_WRITE_DATA) {
      if(dataLen < 4) return;
      int start = (msgData[0] << 8) | msgData[1];
      int length = msgData[2];
      if(length + 3 != dataLen) return;
      i2c_eeprom_write_page(start, msgData + 3, length);
      // send a respose to the host
      cmd = RET_WRITE_ACK;
      dataLen = 0;
      sendMsg();
   }
}

// sends a message
void sendMsg() {
    Serial.print(SOH, BYTE);
    stuffTx(cmd);
    stuffTx(dataLen);
    byte i;
    for(i = 0; i < dataLen; i ++) {
      stuffTx(msgData[i]); 
    }
    Serial.print(EOT, BYTE);
}

// stuffs and sends a byte
void stuffTx(byte data) {
   if(data == SOH) {
      Serial.print(ESC, BYTE);
      Serial.print(ESC_SOH, BYTE);
   } 
   else if(data == EOT) {
      Serial.print(ESC, BYTE);
      Serial.print(ESC_EOT, BYTE);
   } 
   else if(data == ESC) {
      Serial.print(ESC, BYTE);
      Serial.print(ESC_ESC, BYTE);
   }
   else {
      Serial.print(data, BYTE);
   }
}

// writes a page to the EEPROM
void i2c_eeprom_write_page(unsigned int eeaddresspage, byte *buffer, byte length) {
  Wire.beginTransmission(0x50);
  Wire.send((int)(eeaddresspage >> 8)); // MSB
  Wire.send((int)(eeaddresspage & 0xFF)); // LSB
  byte i;
  for(i = 0; i < length; i ++) {
    Wire.send(buffer[i]);
  }
  Wire.endTransmission();
}

// reads a page from the EEPROM
void i2c_eeprom_read_buffer(unsigned int eeaddress, byte *buffer, int length ) {
  Wire.beginTransmission(0x50);
  Wire.send((int)(eeaddress >> 8)); // MSB
  Wire.send((int)(eeaddress & 0xFF)); // LSB
  Wire.endTransmission();
  Wire.requestFrom(0x50, length);
  byte i = 0;
  for(i = 0; i < length; i ++) {
    if (Wire.available()) buffer[i] = Wire.receive();
  }
}


