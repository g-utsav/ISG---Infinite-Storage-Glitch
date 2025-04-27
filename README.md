# Infinite Storage Glitch (ISG) Implementation

The Infinite Storage Glitch (ISG) is a technique that encodes arbitrary files into video frames so they can be uploaded (e.g., to YouTube) as if they were video content. This Java implementation uses the JCodec library to encode and decode files into video frames.

## Features
- **Encoding**: Converts a file into a video by encoding its binary data into pixel blocks.
- **Decoding**: Extracts the original file from the encoded video.
- **Metadata Embedding**: Stores file metadata (e.g., file name, size) in the first frame of the video.
- **Robustness**: Uses block-based encoding to withstand video compression.

## How It Works
1. **Encoding**:
   - Each bit of the file is represented by a block of pixels.
   - Black blocks represent `0`, and white blocks represent `1`.
   - Metadata (e.g., file name, size) is stored in the first frame.
   - Subsequent frames store the file's binary data.

2. **Decoding**:
   - Reads the metadata from the first frame to determine the file name and size.
   - Extracts binary data from subsequent frames to reconstruct the original file.
   
## Requirements
- Java 17 or higher
- Maven for dependency management
- JCodec library (`jcodec-javase`)

## Installation
1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/isg.git
   cd isg

2. Build the project using Maven:

mvn clean package

The compiled JAR file will be located in the target directory:

isg-1.0-SNAPSHOT-jar-with-dependencies.jar
Usage
Run the application using the following command:

Encoding
java -jar target/isg-1.0-SNAPSHOT-jar-with-dependencies.jar encode <input-file> <output-video>

Decoding
java -jar target/isg-1.0-SNAPSHOT-jar-with-dependencies.jar decode <input-video> <output-directory>

Project Structure
Encoder.java: Handles the encoding process (file → video).
Decoder.java: Handles the decoding process (video → file).
ISG.java: Main entry point for the CLI application.
Dependencies
JCodec: A pure Java video codec library.
License
This project is licensed under the MIT License. See the LICENSE file for details.

Acknowledgments
Inspired by the Infinite Storage Glitch (ISG) concept.
Inspired by the following repository written in Rust: DvorakDwarf/Infinite-Storage-Glitch
Built using the JCodec library for video processing.

## Disclaimer

This project is intended purely for educational and learning purposes.  
We do not encourage or endorse any misuse of this technique to violate the terms and conditions of YouTube or any other platforms.  
Any misuse or violation arising from the use of this project is solely the responsibility of the user.  
The author has created this project in good faith for learning purposes only and is not liable for any actions taken by others.
