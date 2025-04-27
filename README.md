# Infinite Storage Glitch (ISG) Implementation

The Infinite Storage Glitch (ISG) is a technique that encodes arbitrary files into video frames so they can be uploaded (e.g., to YouTube) as if they were video content. This Java implementation uses the JCodec library to encode and decode files into video frames.

## Features
- **Encoding**: Converts a file into a video by encoding its binary data into pixel blocks.
- **Decoding**: Extracts the original file from the encoded video.
- **Metadata Embedding**: Stores file metadata (e.g., file name, size) in the first frame of the video.
- **Robustness**: Uses block-based encoding to withstand video compression.

## 🔧 How It Works

ISG operates by converting file data into video frames, allowing the storage of non-video files on video-sharing platforms:

### Data Encoding: 
Files are encoded into video frames using either:
- **RGB Mode**: Each pixel encodes three bytes (one per color channel).
- **Binary Mode**: Each pixel represents a single bit, offering higher resilience against compression artifacts.

### Metadata Embedding: 
The first frame includes metadata (e.g., encoding mode, block size, original file size) to facilitate accurate decoding.

### Compression Resistance: 
To mitigate compression-induced data loss, the tool employs techniques like enlarging pixel blocks (e.g., 2x2) and adding redundancy.

## How Binary Encoding Works

- **Bit Representation**: Each bit of data is a solid square block of pixels. Black (RGB=0,0,0) represents 0, white (255,255,255) represents 1.
- **Block Size**: We use 2×2 pixels per bit by default. This redundancy makes the data robust: YouTube’s compression tends to preserve large uniform blocks even if individual pixels get slightly altered. (Empirically, 2×2 or 4×4 blocks are often used in ISG implementations for resilience.)
- **Frame Capacity**: If the video frame is W×H pixels and block size is 2, each frame carries (W/2) * (H/2) bits. We fill one frame at a time with bits, row by row.
- **Metadata Frame**: The first frame is reserved for metadata about the encoded file. It is also drawn in black/white blocks. The metadata includes a magic string (e.g., "ISG1"), the file length, and the original filename. By convention we put these in the first frame so the decoder knows how to proceed.

### Example:
If we use 1280×720 frames (720p) and block=2, each frame can hold (1280/2)(720/2) = 320×360 = 115,200 bits = 14,400 bytes. A 100 KB file (≈102,400 bytes) would need one metadata frame + 8 data frames.

## Metadata Embedding

We define a simple metadata format. In the first frame, we write the following fields (in order), all as big-endian binary data:
1. **Magic String**: 4 ASCII bytes, e.g. "ISG1". This lets the decoder verify the format.
2. **File Size**: A 32-bit unsigned integer (4 bytes) giving the length of the original file in bytes.
3. **Filename Length**: A 32-bit integer (4 bytes) giving the length of the filename string.
4. **Filename**: UTF-8 bytes of the original file’s name (that many bytes).

Each of these is converted to bits (8 bits per byte) and written into the first frame’s pixel blocks from top-left onward. For example, the letters "I", "S", "G", "1" become 32 bits, followed by the 32-bit size, etc. 

(On decoding, we read the first frame block-by-block, reconstruct those bytes, and parse out the magic string, file length, and filename. If the magic is wrong or data is incomplete, we report an error. Once metadata is parsed, we know exactly how many data bits to read from the remaining frames (fileLength * 8 bits).)

   
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

This project has been developed solely for educational and learning purposes.
It is not intended to promote or encourage the misuse of any platform's terms of service, community guidelines, or content policies.
The creator (me) does not endorse or take any responsibility for the misuse of this tool.
Any usage of this project for violating platform rules, uploading prohibited content, bypassing storage restrictions, or any other unethical/illegal activities is strictly the user's responsibility.
This project was made in good faith as a way to explore creative and technical ideas in data encoding and video processing.
The author has created this project in good faith for learning purposes only and is not liable for any actions taken by others.

Always use technology responsibly. 🙏
