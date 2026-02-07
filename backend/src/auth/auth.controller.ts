import { Controller, Post, Body, Get, UseGuards } from '@nestjs/common';
import { AuthService } from './auth.service';
import { RegisterDto } from './dto/register.dto';
import { LoginDto } from './dto/login.dto';
import { JwtAuthGuard } from './guards/jwt-auth.guard';
import { CurrentUser } from './decorators/current-user.decorator';

@Controller('auth')
export class AuthController {
  constructor(private readonly authService: AuthService) {}

  @Post('register')
  async register(@Body() registerDto: RegisterDto) {
    console.log('🔵 REGISTER REQUEST:', JSON.stringify({ email: registerDto.email, firstName: registerDto.firstName, lastName: registerDto.lastName }, null, 2));
    const result = await this.authService.register(registerDto);
    console.log('🟢 REGISTER RESPONSE:', result ? '✓ Success' : '✗ Failed');
    return result;
  }

  @Post('login')
  async login(@Body() loginDto: LoginDto) {
    console.log('🔵 LOGIN REQUEST:', JSON.stringify(loginDto, null, 2));
    const result = await this.authService.login(loginDto);
    console.log('🟢 LOGIN RESPONSE:', result ? '✓ Success' : '✗ Failed');
    return result;
  }

  @Get('profile')
  @UseGuards(JwtAuthGuard)
  async getProfile(@CurrentUser() user: any) {
    return this.authService.validateUser(user.userId);
  }
}
